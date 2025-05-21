package ru.almaz.server.controller.vote;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.almaz.server.factory.HandlerFactory;
import ru.almaz.server.service.session.VoteCreateSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateVoteStepOptionController implements CreateVoteStepController {

    private final HandlerFactory handlerFactory;

    @Override
    public void accept(VoteCreateSession session, ChannelHandlerContext ctx, String message) {
        if (message.isEmpty())
            ctx.writeAndFlush("Вариант ответа не может быть пустым\n");
        else if (session.canAddOption()) {
            session.addAnswerOptionToVote(message);
            if (!session.canAddOption()) {
                session.addVoteInTopic();
                ctx.writeAndFlush(
                        String.format("Вы создали голосование: %s\n", session.getVote().getName())
                );
                ctx.pipeline().removeLast();
                ctx.pipeline().addLast(handlerFactory.getMainHandler());
                log.info("#{}: Cоздал голосование с именем: {}", ctx.channel().id(), session.getVote().getName());
                return;
            }
            ctx.writeAndFlush(
                    String.format("Введите %s вариант ответа\n", session.getAnswerOptionCount() + 1)
            );
        }
    }

    @Override
    public int getVoteStep() {
        return 4;
    }
}
