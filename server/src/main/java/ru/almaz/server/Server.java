package ru.almaz.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.almaz.server.config.ServerConfig;

import ru.almaz.server.factory.HandlerFactory;
import ru.almaz.server.handler.ServerCommandHandler;

import java.util.Scanner;

import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class Server {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Server.class);

    @Value("${server.port}")
    private int PORT;

    private final HandlerFactory handlerFactory;

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);
        Server server = context.getBean(Server.class);
        server.start();
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
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), handlerFactory.getMainHandler());
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
