package ru.almaz.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class Client {


    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new Handler());
                        }
                    });
            ChannelFuture future = bootstrap.connect("localhost",8080).sync();

            Channel channel = future.channel();

            Scanner scanner = new Scanner(System.in);
            while(true){
                String msg = scanner.nextLine();
                if(msg.equals("exit")){
                    break;
                }
                channel.writeAndFlush(msg);
            }
            scanner.close();
            future.channel().closeFuture().sync();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            group.shutdownGracefully();
        }

    }
}
