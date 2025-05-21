package ru.almaz.server.controller.vote;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.session.VoteCreateSession;

@Component
@Slf4j
public class CreateVoteStepCountOptionController implements CreateVoteStepController{

    @Override
    public void accept(VoteCreateSession session, ChannelHandlerContext ctx, String message) {
        int optionsCount = 0;
        try {
            optionsCount = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            ctx.writeAndFlush("Некорректное значение\n");
            log.warn("#{}: Некорректное значение для количества ответов", ctx.channel().id());
            return;
        }
        if (optionsCount <= 0) {
            ctx.writeAndFlush("Число должно быть больше 0\n");
            log.warn("#{}: Некорректное значение для количества ответов", ctx.channel().id());
        } else {
            session.setOptionsCount(optionsCount);
            session.nextStep();
            ctx.writeAndFlush("Введите 1 вариант ответа\n");
        }
    }

    @Override
    public int getVoteStep() {
        return 3;
    }
}
