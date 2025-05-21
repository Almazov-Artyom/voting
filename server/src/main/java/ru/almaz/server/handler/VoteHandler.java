package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.VoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class VoteHandler extends SimpleChannelInboundHandler<String> {
    private final VoteService voteService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("#{}: Сообщение - {}", ctx.channel().id(), msg);
        voteService.activeCreateVote(ctx, msg);
    }

}
