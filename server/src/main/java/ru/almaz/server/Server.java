package ru.almaz.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.almaz.server.creator.MainHandlerCreator;
import ru.almaz.server.handler.ClientCommandHandler;
import ru.almaz.server.handler.ServerCommandHandler;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.service.LoginService;
import ru.almaz.server.storage.UserStorage;

import java.util.Scanner;


public class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), MainHandlerCreator.create());
                        }
                    });

            ChannelFuture future = bootstrap.bind(PORT).sync();
            System.out.printf("Сервер запущен на порту %d\n", PORT);

            readConsole();

            future.channel().close().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void readConsole() {
        Scanner scanner = new Scanner(System.in);
        ServerCommandHandler consoleHandler = new ServerCommandHandler();
        while (true) {
            String command = scanner.nextLine();
            if (command.matches("^exit$")) {
                break;
            }
            consoleHandler.handleCommand(command);
        }
        scanner.close();
    }
}
