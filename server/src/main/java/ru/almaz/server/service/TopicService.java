package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

@RequiredArgsConstructor
public class TopicService {
    private static final TopicStorage topicStorage = new TopicStorage();

    public static void createTopic(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create topic -n=".length()).trim();
        System.out.println(topicName);
        if (!topicStorage.isTopicExists(topicName)) {
            topicStorage.saveTopic(new Topic(topicName));
        } else
            ctx.writeAndFlush("Топик с таким именем уже существует\n");
    }

    public static void view(ChannelHandlerContext ctx, String msg) {
        topicStorage.findAllTopics().forEach(topic ->
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
        topicStorage.findTopicByName(topicName).ifPresentOrElse(
                topic -> topic.getVotes()
                        .forEach(vote -> ctx.writeAndFlush(vote.getName() + "\n")),
                () -> ctx.writeAndFlush("Такого топика не существует\n")
        );
    }

}
