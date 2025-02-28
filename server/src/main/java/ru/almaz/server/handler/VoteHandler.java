package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import ru.almaz.server.service.VoteService;

@Getter
@Setter
public class VoteHandler extends SimpleChannelInboundHandler<String> {
    private final VoteService voteService = new VoteService();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        voteService.activeCreateVote(ctx,msg);
    }

}
