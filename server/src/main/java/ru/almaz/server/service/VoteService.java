package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.almaz.server.controller.vote.CreateVoteStepController;
import ru.almaz.server.factory.HandlerFactory;
import ru.almaz.server.manager.VoteManager;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;
import ru.almaz.server.service.session.AnswerSession;
import ru.almaz.server.service.session.VoteCreateSession;
import ru.almaz.server.storage.TopicStorage;
import ru.almaz.server.storage.UserStorage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {
    private final Map<Channel, VoteCreateSession> activeCreateSessions = new HashMap<>();

    private final Map<Channel, AnswerSession> activeAnswerSessions = new HashMap<>();

    private final Map<Integer, CreateVoteStepController> createVoteStepControllers = new HashMap<>();

    private final UserStorage userStorage;

    private final TopicStorage topicStorage;

    private final VoteManager voteManager;

    private final HandlerFactory handlerFactory;

    private Optional<Pair<Topic, Vote>> getTopicAndVote(ChannelHandlerContext ctx, String msg) {
        String[] parts = msg.split(" -t=| -v=");
        String topicName = parts[1].trim();
        String voteName = parts[2].trim();

        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);
        if (topic == null) {
            ctx.writeAndFlush("Такого топика не существует\n");
            log.warn("#{}: Топик с именем: {} не существует", ctx.channel().id(), topicName);
            return Optional.empty();
        }

        Vote vote = voteManager.findVoteByTopic(topic, voteName).orElse(null);
        if (vote == null) {
            ctx.writeAndFlush("Такого голосования не существует\n");
            log.warn("#{}: Голосования с именем: {} не существует", ctx.channel().id(), voteName);
            return Optional.empty();
        }

        return Optional.of(Pair.of(topic, vote));
    }

    public void register(CreateVoteStepController createVoteStepController) {
        createVoteStepControllers.put(createVoteStepController.getVoteStep(), createVoteStepController);
    }

    public void startCreateVote(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create vote -t=".length()).trim();

        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);

        if (topic == null) {
            ctx.writeAndFlush("Такого топика не существует\n");
            log.warn("#{}: Топик с именем:{} не существует",ctx.channel().id(), topicName);
        } else {
            activeCreateSessions.put(ctx.channel(), new VoteCreateSession(topic));
            ctx.writeAndFlush("Введите название голосования\n");
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(handlerFactory.getVoteHandler());
        }
    }

    public void activeCreateVote(ChannelHandlerContext ctx, String msg) {
        VoteCreateSession session = activeCreateSessions.get(ctx.channel());
        CreateVoteStepController createVoteStepController = createVoteStepControllers.get(session.getStep());
        createVoteStepController.accept(session,ctx,msg);
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
                log.warn("#{}: уже отвечал на голосование: {}", ctx.channel().id(), vote.getName());
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
            ctx.pipeline().addLast(handlerFactory.getAnswerHandler());
        });
    }

    public void activeVote(ChannelHandlerContext ctx, String msg) {
        AnswerSession answerSession = activeAnswerSessions.get(ctx.channel());
        int numberAnswer = 0;

        try {
            numberAnswer = Integer.parseInt(msg);
        } catch (NumberFormatException e) {
            ctx.writeAndFlush("Недопустимое значение\n");
            log.warn("#{}: Недопустимое значение для ответа", ctx.channel().id());
            return;
        }

        if (numberAnswer > 0 && numberAnswer <= answerSession.getCountAnswers()) {
            answerSession.toAnswer(numberAnswer, userStorage.findUserByChannel(ctx.channel()));
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(handlerFactory.getMainHandler());
            ctx.writeAndFlush(String.format("Вы выбрали %d вариант\n", numberAnswer));
            log.info("#{}: Выбрал {}Вариант ответа для голосования: {}", ctx.channel().id(), numberAnswer, answerSession.getVoteName());
        } else {
            ctx.writeAndFlush("Такого ответа нет\n");
            log.warn("#{} Варианта {} нет", ctx.channel().id(), numberAnswer);
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
                log.warn("#{}Голосование может удалить только пользователь, создавший его", ctx.channel().id());
            }
        });
    }
}
