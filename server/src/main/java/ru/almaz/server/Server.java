package ru.almaz.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.almaz.server.creator.MainHandlerCreator;
import ru.almaz.server.handler.ServerCommandHandler;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Server {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Server.class);

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

            logger.info(String.format("Сервер запущен на порту %d", PORT));

            readConsole();

            future.channel().close().sync();

        } catch (Exception e) {
            logger.error(e.toString());
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
