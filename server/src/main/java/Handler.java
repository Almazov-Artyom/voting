import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import model.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Handler extends SimpleChannelInboundHandler<String> {
    private static final Map<Channel,String> users = new HashMap<>();

    private static final List<Topic> topics = new ArrayList<>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user connected" + ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
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
                topics.forEach(topic ->
                        ctx.writeAndFlush(topic.getName()+String.format(" (votes in topic = %s)",topic.getVotes().size())));
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
