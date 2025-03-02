package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.service.VoteService;

@RequiredArgsConstructor
public class VoteHandler extends SimpleChannelInboundHandler<String> {

    private final VoteService voteService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        voteService.activeCreateVote(ctx, msg);
    }

}
