package ru.almaz.server.controller;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import ru.almaz.server.service.TopicService;

@Component
@RequiredArgsConstructor
public class CreateTopicCommandController implements CommandController {

    @Value("${client.template.command.create.topic}")
    private String createTopicTemplate;

    private final TopicService topicService;

    @Override
    public void accept(ChannelHandlerContext ctx, String message) {
        topicService.createTopic(ctx, message);
    }

    @Override
    public String getTemplateCommand() {
        return createTopicTemplate;
    }
}
