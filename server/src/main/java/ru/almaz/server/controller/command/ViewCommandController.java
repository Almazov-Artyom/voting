package ru.almaz.server.controller.command;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.TopicService;

@Component
@RequiredArgsConstructor
public class ViewCommandController implements CommandController {
    @Value("${client.template.command.view}")
    private String viewTemplate;

    private final TopicService topicService;

    @Override
    public void accept(ChannelHandlerContext ctx, String message) {
        topicService.view(ctx, message);
    }

    @Override
    public String getTemplateCommand() {
        return viewTemplate;
    }
}
