package ru.almaz.server.controller.vote;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.almaz.server.manager.VoteManager;
import ru.almaz.server.service.VoteService;
import ru.almaz.server.service.session.VoteCreateSession;
import ru.almaz.server.storage.UserStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateVoteStepNameController implements CreateVoteStepController{
    private final VoteManager voteManager;

    private final UserStorage userStorage;

    @Override
    public void accept(VoteCreateSession session, ChannelHandlerContext ctx, String message) {
        if (message.isEmpty())
            ctx.writeAndFlush("Название голосования не может быть пустым\n");
        else if (voteManager.isVoteInTopicExists(session.getTopic(), message)) {
            ctx.writeAndFlush("Такое голосование уже есть\n");
            log.warn("#{}: Голосования с именем: {} уже есть", ctx.channel().id(), session.getVote().getName());
        } else {
            session.setVoteName(message);
            session.setVoteUserCreator(userStorage.findUserByChannel(ctx.channel()));
            session.nextStep();
            ctx.writeAndFlush("Введите тему голосования\n");
        }
    }

    @Override
    public int getVoteStep() {
        return 1;
    }
}
