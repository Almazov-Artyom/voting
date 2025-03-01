package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.handler.AnswerHandler;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.handler.VoteHandler;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;
import ru.almaz.server.service.session.AnswerSession;
import ru.almaz.server.service.session.VoteCreateSession;
import ru.almaz.server.storage.TopicStorage;
import ru.almaz.server.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class VoteService {

    private static final TopicStorage topicStorage = new TopicStorage();

    private static final UserStorage userStorage = new UserStorage();

    private static final Map<Channel, VoteCreateSession> activeCreateSessions = new HashMap<>();

    private static final Map<Channel, AnswerSession> activeAnswerSessions = new HashMap<>();

    private static Optional<Vote> getVoteFromTopic(ChannelHandlerContext ctx, String msg) {
        String[] parts = msg.split(" -t=| -v=");
        String topicName = parts[1].trim();
        String voteName = parts[2].trim();
        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);
        if (topic != null) {
            Vote vote = topicStorage.findVoteByTopic(topic, voteName).orElse(null);
            if (vote == null) {
                ctx.writeAndFlush("Такого голосования не существует\n");
            } else {
                return Optional.of(vote);
            }
        } else {
            ctx.writeAndFlush("Такого топика не существует\n");
        }
        return Optional.empty();
    }

    public static void startCreateVote(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create vote -t=".length()).trim();

        if (!topicStorage.isTopicExists(topicName)) {
            ctx.writeAndFlush("Такого топика не существует\n");
        } else {
            Topic topic = topicStorage.findTopicByName(topicName).orElse(null);
            activeCreateSessions.put(ctx.channel(), new VoteCreateSession(topic));
            ctx.writeAndFlush("Введите название голосования\n");
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new VoteHandler());
        }
    }

    public static void activeCreateVote(ChannelHandlerContext ctx, String msg) {
        VoteCreateSession session = activeCreateSessions.get(ctx.channel());
        switch (session.getStep()) {
            case 0:
                if (msg.isEmpty())
                    ctx.writeAndFlush("Название голосования не может быть пустым\n");
                else if (topicStorage.isVoteInTopicExists(session.getTopic(), msg)) {
                    ctx.writeAndFlush("Такое голосование уже есть\n");
                } else {
                    session.setVoteName(msg);
                    session.setVoteUserCreator(userStorage.findUserByChannel(ctx.channel()));
                    session.nextStep();
                    ctx.writeAndFlush("Введите тему голосования\n");
                }
                break;
            case 1:
                if (msg.isEmpty())
                    ctx.writeAndFlush("Тема голосования не может быть пустой\n");
                else {
                    session.setVoteDescription(msg);
                    session.nextStep();
                    ctx.writeAndFlush("Введите количество вариантов ответов\n");
                }
                break;
            case 2:
                int optionsCount = 0;
                try {
                    optionsCount = Integer.parseInt(msg);
                } catch (NumberFormatException e) {
                    ctx.writeAndFlush("Некорректное значение\n");
                    return;
                }
                if (optionsCount <= 0) {
                    ctx.writeAndFlush("Число должно быть больше 0\n");
                } else {
                    session.setOptionsCount(optionsCount);
                    session.nextStep();
                    ctx.writeAndFlush("Введите 1 вариант ответа\n");
                }
                break;
            case 3:
                if (msg.isEmpty())
                    ctx.writeAndFlush("Вариант ответа не может быть пустым\n");
                else if (session.canAddOption()) {
                    session.addAnswerOptionToVote(msg);
                    if (!session.canAddOption()) {
                        ctx.pipeline().removeLast();
                        ctx.pipeline().addLast(new MainHandler());
                        session.addVoteInTopic();
                        break;
                    }
                    ctx.writeAndFlush(
                            String.format("Введите %s вариант ответа\n", session.getAnswerOptionCount() + 1)
                    );
                }
                break;
        }
    }

    public static void view(ChannelHandlerContext ctx, String msg) {
        getVoteFromTopic(ctx, msg).ifPresent(vote -> {
            ctx.writeAndFlush(vote.getDescription() + "\n");
            vote.getAnswerOptions().forEach(answerOption ->
                    ctx.writeAndFlush(
                            String.format("%s (%d пользователей выбрали это вариант)\n",
                                    answerOption.getAnswer(), answerOption.getCountUsers()
                            )
                    )
            );
        });
    }

    public static void startVote(ChannelHandlerContext ctx, String msg) {
        getVoteFromTopic(ctx, msg).ifPresent(vote -> {
            if (vote.getAnswerUsers().contains(userStorage.findUserByChannel(ctx.channel()))) {
                ctx.writeAndFlush("Вы уже отвечали\n");
                return;
            }
            AtomicInteger numberAnswer = new AtomicInteger(1);
            vote.getAnswerOptions().forEach(answerOption ->
                    ctx.writeAndFlush(
                            String.format("%d. %s\n",
                                    numberAnswer.getAndIncrement(), answerOption.getAnswer()
                            )
                    )
            );
            ctx.writeAndFlush("Выберите ответ\n");
            activeAnswerSessions.put(ctx.channel(), new AnswerSession(vote));
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new AnswerHandler());
        });
    }

    public static void activeVote(ChannelHandlerContext ctx, String msg) {
        AnswerSession answerSession = activeAnswerSessions.get(ctx.channel());
        int numberAnswer = 0;

        try {
            numberAnswer = Integer.parseInt(msg);
        } catch (NumberFormatException e) {
            ctx.writeAndFlush("Недопустимое значение\n");
            return;
        }

        if (numberAnswer > 0 && numberAnswer <= answerSession.getCountAnswers()) {
            answerSession.toAnswer(numberAnswer, userStorage.findUserByChannel(ctx.channel()));
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new MainHandler());
        } else {
            ctx.writeAndFlush("Такого ответа нет\n");
        }
    }

    public static void deleteVote(ChannelHandlerContext ctx, String msg) {
        String[] parts = msg.split(" -t=| -v=");
        String topicName = parts[1].trim();
        String voteName = parts[2].trim();
        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);
        if (topic == null) {
            ctx.writeAndFlush("Такого топика не существует\n");
            return;
        }
        Vote vote = topicStorage.findVoteByTopic(topic, voteName).orElse(null);
        if (vote == null) {
            ctx.writeAndFlush("Такого голосования не существует\n");
            return;
        }
        if (vote.getUserCreator().equals(userStorage.findUserByChannel(ctx.channel()))) {
            topic.getVotes().remove(vote);
        } else {
            ctx.writeAndFlush("Голосование может удалить только пользователь, создавший его\n");
        }

    }

}
