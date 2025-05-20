package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.VoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class VoteHandler extends SimpleChannelInboundHandler<String> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VoteHandler.class);

    private final VoteService voteService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info("#" + ctx.channel().id() + ": Сообщение - " + msg);
        voteService.activeCreateVote(ctx, msg);
    }

}
