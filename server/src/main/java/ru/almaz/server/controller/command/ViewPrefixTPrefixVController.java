package ru.almaz.server.controller.command;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.VoteService;

@Component
@RequiredArgsConstructor
public class ViewPrefixTPrefixVController implements CommandController{
    @Value("${client.template.command.view.prefixT.prefixV}")
    private String viewTemplate;

    private final VoteService voteService;

    @Override
    public void accept(ChannelHandlerContext ctx, String message) {
        voteService.view(ctx, message);
    }

    @Override
    public String getTemplateCommand() {
        return viewTemplate;
    }
}
