package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.ClientCommandService;
import ru.almaz.server.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class MainHandler extends SimpleChannelInboundHandler<String> {
    private final ClientCommandService clientCommandService;

    private final LoginService loginService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("#{}: Пользователь подключился", ctx.channel().id());
        ctx.writeAndFlush("Вы подключены\nДля продолжения войдите\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("#{}: Пользователь отключился", ctx.channel().id());
        loginService.logout(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("#{}: Сообщение - {}", ctx.channel().id(), msg);
        clientCommandService.processingCommand(ctx, msg.trim());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.toString());
        ctx.close();
    }


}
