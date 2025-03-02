package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.service.LoginService;

@RequiredArgsConstructor
public class MainHandler extends SimpleChannelInboundHandler<String> {
    private final ClientCommandHandler commandHandler;

    private final LoginService loginService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user connected" + ctx);
        ctx.writeAndFlush("Вы подключены\nДля продолжения войдите\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user disconnected");
        loginService.logout(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
        commandHandler.handleCommand(ctx, msg.trim());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
