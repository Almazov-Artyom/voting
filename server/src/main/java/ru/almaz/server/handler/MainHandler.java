package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.almaz.server.service.LoginService;
import ru.almaz.server.service.TopicService;
import ru.almaz.server.service.VoteService;


public class MainHandler extends SimpleChannelInboundHandler<String> {

    private final LoginService loginService = new LoginService();

    private final TopicService topicService = new TopicService();

    private final VoteService voteService = new VoteService();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user connected" + ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("user disconnected");
        loginService.logout(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
        System.out.println("pipline:"+ctx.pipeline());

        if(!loginService.isLoggedIn(ctx.channel())) {
            if(msg.startsWith("login -u=")){
                loginService.loginCommand(ctx, msg);
            } else
                ctx.writeAndFlush("Вы не залогинились");
        }
        else{
            if(msg.startsWith("create topic -n=")){
                topicService.createTopicCommand(ctx, msg);
            }
            else if(msg.startsWith("view")){
                if(msg.matches("^view -t=[^ ]+ -v=.+$"))
                    voteService.view(ctx, msg);
                else
                    topicService.viewCommand(ctx, msg);
            }
            else if(msg.startsWith("create vote -t=")){
                voteService.startCreateVote(ctx, msg);
            }

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }



}
