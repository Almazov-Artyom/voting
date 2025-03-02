package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class TopicService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TopicService.class);

    private final TopicStorage topicStorage;

    public void createTopic(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create topic -n=".length()).trim();
        if (!topicStorage.isTopicExists(topicName)) {
            topicStorage.saveTopic(new Topic(topicName));
            ctx.writeAndFlush(String.format("Вы создали топик с именем: %s\n", topicName));
            logger.info("#" + ctx.channel().id() + ": Создал топик с именем: " + topicName);
        } else {
            ctx.writeAndFlush("Топик с таким именем уже существует\n");
            logger.warn("#" + ctx.channel().id() + ": Топик с именем: " + topicName + " уже есть");
        }

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
        topicStorage.findTopicByName(topicName).ifPresentOrElse(
                topic -> topic.getVotes()
                        .forEach(vote -> ctx.writeAndFlush(vote.getName() + "\n")),
                () -> {
                    ctx.writeAndFlush("Такого топика не существует\n");
                    logger.warn("#" + ctx.channel().id() + ": Топика с именем: " + topicName + " не существует");

                }
        );
    }
}
