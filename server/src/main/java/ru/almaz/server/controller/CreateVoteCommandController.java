package ru.almaz.server.controller;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.VoteService;

@Component
@RequiredArgsConstructor
public class CreateVoteCommandController implements CommandController {
    @Value("${client.template.command.create.vote}")
    private String createVoteTemplate;

    private final VoteService voteService;

    @Override
    public void accept(ChannelHandlerContext ctx, String message) {
        voteService.startCreateVote(ctx, message);
    }

    @Override
    public String getTemplateCommand() {
        return createVoteTemplate;
    }
}
