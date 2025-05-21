package ru.almaz.server.controller.vote;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.almaz.server.service.VoteService;
import ru.almaz.server.service.session.VoteCreateSession;

public interface CreateVoteStepController {
    void accept(VoteCreateSession session, ChannelHandlerContext ctx, String message);

    int getVoteStep();

    @Autowired
    default void register(VoteService voteService) {
        voteService.register(this);
    }
}
