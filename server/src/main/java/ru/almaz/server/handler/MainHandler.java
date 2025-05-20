package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainHandler.class);
    private final ClientCommandHandler commandHandler;

    private final LoginService loginService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("#" + ctx.channel().id() + ": Пользователь подключился");
        ctx.writeAndFlush("Вы подключены\nДля продолжения войдите\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("#" + ctx.channel().id() + ": Пользователь отключился");
        loginService.logout(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info("#" + ctx.channel().id() + ": Сообщение - " + msg);
        commandHandler.handleCommand(ctx, msg.trim());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.toString());
        ctx.close();
    }


}
