package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

@Getter
@Setter
public class VoteHandler extends SimpleChannelInboundHandler<String> {

    private int step;

    private int optionsCount;

    private Topic topic;

    private final MainHandler mainHandler;

    private final Vote vote = new Vote();

    public VoteHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        switch (step) {
            case 0:
                String voteName = msg;
                if(topic.getVotes().stream().anyMatch(vote -> vote.getName().equals(voteName))) {
                    ctx.writeAndFlush("Такое голосование уже есть");
                }
                else{
                    vote.setName(voteName);
                    step++;
                    ctx.writeAndFlush("Введите тему голосования");
                }
                break;
            case 1:
                vote.setDescription(msg);
                step++;
                ctx.writeAndFlush("Введите количество вариантов ответов");
                break;
            case 2:
                optionsCount = Integer.parseInt(msg);
                step++;
                ctx.writeAndFlush("Введите 1 вариант ответа");
                break;
            case 3:
                if(vote.getAnswerOptions().size() < optionsCount) {
                    vote.getAnswerOptions().add(new Vote.AnswerOption(msg));
                    if(vote.getAnswerOptions().size() == optionsCount){
                        ctx.pipeline().remove(this);
                        ctx.pipeline().addLast(new MainHandler());
                        break;
                    }

                    ctx.writeAndFlush(String.format("Введите %s вариант ответа",vote.getAnswerOptions().size()+1));
                }
        }

    }

}
