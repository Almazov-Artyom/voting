package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

import java.util.List;

@RequiredArgsConstructor
public class TopicService {

    private final TopicStorage topicStorage;

    public void createTopic(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create topic -n=".length()).trim();
        System.out.println(topicName);
        if (!topicStorage.isTopicExists(topicName)) {
            topicStorage.saveTopic(new Topic(topicName));
            ctx.writeAndFlush(String.format("Вы создали топик с именем: %s\n", topicName));
        } else
            ctx.writeAndFlush("Топик с таким именем уже существует\n");
    }

    public void view(ChannelHandlerContext ctx, String msg) {
        List<Topic> topics = topicStorage.findAllTopics();
        if (topics.isEmpty()) {
            ctx.writeAndFlush("Топиков нет\n");
            return;
        }
        for (Topic topic : topics) {
            ctx.writeAndFlush(
                    String.format("%s (votes in topic = %d)\n", topic.getName(), topic.getVotes().size())
            );
        }
    }

    public void viewPrefixT(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("view -t=".length()).trim();
        System.out.println("viewprefix:" + topicName);
        topicStorage.findTopicByName(topicName).ifPresentOrElse(
                topic -> topic.getVotes()
                        .forEach(vote -> ctx.writeAndFlush(vote.getName() + "\n")),
                () -> ctx.writeAndFlush("Такого топика не существует\n")
        );
    }
}
