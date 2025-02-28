package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

@RequiredArgsConstructor
public class TopicService {
    private final TopicStorage topicStorage = new TopicStorage();

    public void createTopicCommand(ChannelHandlerContext ctx, String msg) {
        String topicName = msg.substring("create topic -n=".length());
        if(!topicStorage.isTopicExists(topicName)) {
            topicStorage.saveTopic(new Topic(topicName));
        }
        else
            ctx.writeAndFlush("Топик с таким именем уже существует");
    }

    public void viewCommand(ChannelHandlerContext ctx, String msg) {
        if(msg.contains("-t=")){
            String topicName = msg.substring("view -t=".length());
            topicStorage.findTopicByName(topicName)
                    .ifPresentOrElse(
                            topic -> topic.getVotes()
                                    .forEach(vote -> ctx.writeAndFlush(vote.getName())),
                            () -> ctx.writeAndFlush("Такого топика не существует")
                    );
        } else
            topicStorage.findAllTopics().forEach(topic ->
                    ctx.writeAndFlush(topic.getName()+String.format(" (votes in topic = %s)",topic.getVotes().size())));

    }


}
