package ru.almaz.server.factory;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import ru.almaz.server.handler.AnswerHandler;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.handler.VoteHandler;

@Component
public abstract class HandlerFactory {

    @Lookup
    public abstract MainHandler getMainHandler();

    @Lookup
    public abstract AnswerHandler getAnswerHandler();

    @Lookup
    public abstract VoteHandler getVoteHandler();
}
