package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.service.VoteService;

@RequiredArgsConstructor
public class AnswerHandler extends SimpleChannelInboundHandler<String> {
    private final VoteService voteService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        voteService.activeVote(ctx, msg);
    }
}
