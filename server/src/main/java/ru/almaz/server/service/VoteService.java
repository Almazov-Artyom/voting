package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.handler.VoteHandler;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;
import ru.almaz.server.service.session.VoteCreateSession;
import ru.almaz.server.storage.TopicStorage;
import ru.almaz.server.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class VoteService {

    private final TopicStorage topicStorage = new TopicStorage();

    private final UserStorage userStorage = new UserStorage();

    private static final Map<Channel, VoteCreateSession> activeCreateSessions = new HashMap<>();


    public void startCreateVote(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create vote -t=".length());

        if(!topicStorage.isTopicExists(topicName)) {
            ctx.writeAndFlush("Такого топика не существует");
        }
        else {
            Topic topic = topicStorage.findTopicByName(topicName).orElse(null);
            activeCreateSessions.put(ctx.channel(), new VoteCreateSession(topic));
            ctx.writeAndFlush("Введите название голосования");
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new VoteHandler());
        }
    }

    public void activeCreateVote(ChannelHandlerContext ctx, String msg) {
        VoteCreateSession session = activeCreateSessions.get(ctx.channel());
        switch (session.getStep()) {
            case 0:
                if(msg.isEmpty())
                    ctx.writeAndFlush("Название топика не может быть пустым");
                else if(topicStorage.isVoteInTopicExists(session.getTopic(), msg)) {
                    ctx.writeAndFlush("Такое голосование уже есть");
                }
                else{
                    session.setVoteName(msg);
                    session.setVoteUserCreator(userStorage.findUserByChannel(ctx.channel()));
                    session.nextStep();
                    ctx.writeAndFlush("Введите тему голосования");
                }
                break;
            case 1:
                if(msg.isEmpty())
                    ctx.writeAndFlush("Тема голосования не может быть пустой");
                else {
                    session.setVoteDescription(msg);
                    session.nextStep();
                    ctx.writeAndFlush("Введите количество вариантов ответов");
                }
                break;
            case 2:
                int optionsCount = 0;
                try {
                    optionsCount = Integer.parseInt(msg);
                }
                catch (NumberFormatException e) {
                    ctx.writeAndFlush("Некорректное значение");
                    return;
                }
                if (optionsCount <= 0) {
                    ctx.writeAndFlush("Число должно быть больше 0");
                }
                else {
                    session.setOptionsCount(optionsCount);
                    session.nextStep();
                    ctx.writeAndFlush("Введите 1 вариант ответа");
                }
                break;
            case 3:
                if(msg.isEmpty())
                    ctx.writeAndFlush("Вариант ответа не может быть пустым");
                else if(session.canAddOption()) {
                   session.addAnswerOptionToVote(msg);
                    if(!session.canAddOption()){
                        ctx.pipeline().removeLast();
                        ctx.pipeline().addLast(new MainHandler());
                       session.addVoteInTopic();
                        break;
                    }
                    ctx.writeAndFlush(
                            String.format("Введите %s вариант ответа", session.getAnswerOptionCount() + 1)
                    );
                }
        }

    }

    public void view (ChannelHandlerContext ctx, String msg) {
        String[] parts = msg.split(" -t=| -v=");
        String topicName = parts[1];
        String voteName = parts[2];
        System.out.println("название топика voteservice" + topicName);
        Topic topic = topicStorage.findTopicByName(topicName).orElse(null);

        if(topic != null) {
            Vote vote = topicStorage.findVoteByTopic(topic, voteName).orElse(null);
            if(vote == null) {
                ctx.writeAndFlush("Такого голосования не существует");
            }
            else{
                ctx.writeAndFlush(vote.getDescription());
                vote.getAnswerOptions().forEach(answerOption -> ctx.writeAndFlush(
                        answerOption.getAnswer()
                                + String.format(" (%s пользователей выбрали этот вариант)",answerOption.getCountUsers())
                ));
            }
        }
        else {
            ctx.writeAndFlush("Такого топика не существует");
        }

    }

}
