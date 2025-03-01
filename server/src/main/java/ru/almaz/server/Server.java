package ru.almaz.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Value;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.storage.TopicStorage;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
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
                            ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8), new StringEncoder(StandardCharsets.UTF_8), new MainHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(PORT).sync();
            System.out.println("Server started on port 8080");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if (line.equals("exit")) {
                    break;
                }
                TopicStorage.saveTopicsToFile(line);
            }
            scanner.close();
            future.channel().close().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
