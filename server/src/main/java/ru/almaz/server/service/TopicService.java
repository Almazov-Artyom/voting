package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {
    private final TopicStorage topicStorage;

    public void createTopic(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create topic -n=".length()).trim();
        if (!topicStorage.isTopicExists(topicName)) {
            topicStorage.saveTopic(new Topic(topicName));
            ctx.writeAndFlush(String.format("Вы создали топик с именем: %s\n", topicName));
            log.info("#{}: Создал топик с именем: {}", ctx.channel().id(), topicName);
        } else {
            ctx.writeAndFlush("Топик с таким именем уже существует\n");
            log.warn("#{}: Топик с именем: {} уже есть", ctx.channel().id(), topicName);
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
                    log.warn("#{}: Топика с именем: {} не существует", ctx.channel().id(), topicName);

                }
        );
    }
}
