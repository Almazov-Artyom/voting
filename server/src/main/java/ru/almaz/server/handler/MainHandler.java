package ru.almaz.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import ru.almaz.server.model.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final Map<Channel,String> users = new HashMap<>();

    @Getter
    private static final List<Topic> topics = new ArrayList<>();

    private final VoteHandler voteHandler = new VoteHandler(this);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user connected" + ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user disconnected");
        users.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
        if(!users.containsKey(ctx.channel())) {
            if(msg.startsWith("login -u=")){
                String username = msg.substring("login -u=".length());
                loginCommand(ctx,username);
            } else
                ctx.writeAndFlush("Вы не залогинились");
        }
        else{
            if(msg.startsWith("create topic -n=")){
                String topicName = msg.substring("create topic -n=".length());
                if(topics.stream().noneMatch(topic -> topic.getName().equals(topicName) )){
                    topics.add(new Topic(topicName));
                }
                else
                    ctx.writeAndFlush("Топик с таким именем уже существует");
            }
            if(msg.startsWith("view")){
                if(msg.contains("-t=")){
                    String topicName = msg.substring("view -t=".length());
                    topics.forEach(topic -> {
                        if(topic.getName().equals(topicName)){
                            topic.getVotes().forEach(vote -> {
                                ctx.writeAndFlush(vote.getName());
                            });
                        }
                    });
                } else
                    topics.forEach(topic ->
                        ctx.writeAndFlush(topic.getName()+String.format(" (votes in topic = %s)",topic.getVotes().size())));
            }
            if(msg.startsWith("create vote -t=")){
                String topicName = msg.substring("create vote -t=".length());
                if(topics.stream().noneMatch(topic -> topic.getName().equals(topicName) )){
                    ctx.writeAndFlush("Такого топика не существует");
                    return;
                }
                topics.forEach(topic ->{
                    if(topic.getName().equals(topicName)){
                        voteHandler.setTopic(topic);
                    }
                });
                ctx.writeAndFlush("Введите название голосования");
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(voteHandler);
                topics.forEach(topic ->{
                    if(topic == voteHandler.getTopic())
                        topic.getVotes().add(voteHandler.getVote());
                });
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void loginCommand(ChannelHandlerContext ctx, String username) {
        if(!users.containsValue(username)){
            users.put(ctx.channel(),username);
            ctx.writeAndFlush("Вы вошли под именем: "+username);
        } else
            ctx.writeAndFlush("Пользователь с таким именем уже вошел");
    }


}
