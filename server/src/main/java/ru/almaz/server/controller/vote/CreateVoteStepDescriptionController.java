package ru.almaz.server.controller.vote;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.session.VoteCreateSession;

@Component
public class CreateVoteStepDescriptionController implements CreateVoteStepController {

    @Override
    public void accept(VoteCreateSession session, ChannelHandlerContext ctx, String message) {
        if (message.isEmpty())
            ctx.writeAndFlush("Тема голосования не может быть пустой\n");
        else {
            session.setVoteDescription(message);
            session.nextStep();
            ctx.writeAndFlush("Введите количество вариантов ответов\n");
        }
    }

    @Override
    public int getVoteStep() {
        return 2;
    }
}
