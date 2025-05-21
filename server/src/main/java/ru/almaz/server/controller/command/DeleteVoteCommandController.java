package ru.almaz.server.controller.command;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.VoteService;

@Component
@RequiredArgsConstructor
public class DeleteVoteCommandController implements CommandController {
    @Value("${client.template.command.delete.vote}")
    private String deleteVoteTemplate;

    private final VoteService voteService;

    @Override
    public void accept(ChannelHandlerContext ctx, String message) {
        voteService.deleteVote(ctx, message);
    }

    @Override
    public String getTemplateCommand() {
        return deleteVoteTemplate;
    }
}
