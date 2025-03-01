package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;


public class TopicService {

    public static void createTopic(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create topic -n=".length()).trim();
        System.out.println(topicName);
        if (!TopicStorage.isTopicExists(topicName)) {
            TopicStorage.saveTopic(new Topic(topicName));
        } else
            ctx.writeAndFlush("Топик с таким именем уже существует\n");
    }

    public static void view(ChannelHandlerContext ctx, String msg) {
        TopicStorage.findAllTopics().forEach(topic ->
                ctx.writeAndFlush(
                        String.format("%s (votes in topic = %d)\n",
                                topic.getName(), topic.getVotes().size()
                        )
                )
        );
    }

    public static void viewPrefixT(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("view -t=".length()).trim();
        System.out.println("viewprefix:" + topicName);
        TopicStorage.findTopicByName(topicName).ifPresentOrElse(
                topic -> topic.getVotes()
                        .forEach(vote -> ctx.writeAndFlush(vote.getName() + "\n")),
                () -> ctx.writeAndFlush("Такого топика не существует\n")
        );
    }
}
