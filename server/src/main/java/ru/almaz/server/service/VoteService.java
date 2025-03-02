package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import ru.almaz.server.creator.MainHandlerCreator;
import ru.almaz.server.handler.AnswerHandler;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.handler.VoteHandler;
import ru.almaz.server.manager.VoteManager;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;
import ru.almaz.server.service.session.AnswerSession;
import ru.almaz.server.service.session.VoteCreateSession;
import ru.almaz.server.storage.TopicStorage;
import ru.almaz.server.storage.UserStorage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class VoteService {

    private static final Map<Channel, VoteCreateSession> activeCreateSessions = new HashMap<>();

    private static final Map<Channel, AnswerSession> activeAnswerSessions = new HashMap<>();

    private final UserStorage userStorage;

    private final TopicStorage topicStorage;

    private final VoteManager voteManager;

    private Optional<Pair<Topic, Vote>> getTopicAndVote(ChannelHandlerContext ctx, String msg) {
        String[] parts = msg.split(" -t=| -v=");
        String topicName = parts[1].trim();
        String voteName = parts[2].trim();

        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);
        if (topic == null) {
            ctx.writeAndFlush("Такого топика не существует\n");
            return Optional.empty();
        }

        Vote vote = voteManager.findVoteByTopic(topic, voteName).orElse(null);
        if (vote == null) {
            ctx.writeAndFlush("Такого голосования не существует\n");
            return Optional.empty();
        }

        return Optional.of(Pair.of(topic, vote));
    }

    public void startCreateVote(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create vote -t=".length()).trim();

        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);

        if (topic == null) {
            ctx.writeAndFlush("Такого топика не существует\n");
        } else {
            activeCreateSessions.put(ctx.channel(), new VoteCreateSession(topic));
            ctx.writeAndFlush("Введите название голосования\n");
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new VoteHandler(this));
        }
    }

    public void activeCreateVote(ChannelHandlerContext ctx, String msg) {
        VoteCreateSession session = activeCreateSessions.get(ctx.channel());
        switch (session.getStep()) {
            case 0:
                if (msg.isEmpty())
                    ctx.writeAndFlush("Название голосования не может быть пустым\n");
                else if (voteManager.isVoteInTopicExists(session.getTopic(), msg)) {
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
                        ctx.pipeline().addLast(MainHandlerCreator.create());
                        session.addVoteInTopic();
                        ctx.writeAndFlush(
                                String.format("Вы создали голосование: %s\n", session.getVote().getName())
                        );
                        break;
                    }
                    ctx.writeAndFlush(
                            String.format("Введите %s вариант ответа\n", session.getAnswerOptionCount() + 1)
                    );
                }
                break;
        }
    }

    public void view(ChannelHandlerContext ctx, String msg) {
        getTopicAndVote(ctx, msg).ifPresent(pair -> {
            Vote vote = pair.getRight();
            ctx.writeAndFlush(vote.getDescription() + "\n");
            vote.getAnswerOptions().forEach(answerOption ->
                    ctx.writeAndFlush(
                            String.format("%s (%d пользователей выбрали этот вариант)\n",
                                    answerOption.getAnswer(), answerOption.getCountUsers()
                            )
                    )
            );
        });
    }

    public void startVote(ChannelHandlerContext ctx, String msg) {
        getTopicAndVote(ctx, msg).ifPresent(pair -> {
            Vote vote = pair.getRight();
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
            ctx.pipeline().addLast(new AnswerHandler(this));
        });
    }

    public void activeVote(ChannelHandlerContext ctx, String msg) {
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
            ctx.pipeline().addLast(MainHandlerCreator.create());
            ctx.writeAndFlush(String.format("Вы выбрали %d вариант\n", numberAnswer));
        } else {
            ctx.writeAndFlush("Такого ответа нет\n");
        }
    }

    public void deleteVote(ChannelHandlerContext ctx, String msg) {
        getTopicAndVote(ctx, msg).ifPresent(pair -> {
            Topic topic = pair.getLeft();
            Vote vote = pair.getRight();
            if (vote.getUserCreator().equals(userStorage.findUserByChannel(ctx.channel()))) {
                topic.getVotes().remove(vote);
            } else {
                ctx.writeAndFlush("Голосование может удалить только пользователь, создавший его\n");
            }
        });
    }
}
