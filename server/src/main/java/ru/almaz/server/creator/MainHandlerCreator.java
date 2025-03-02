package ru.almaz.server.creator;

import ru.almaz.server.handler.ClientCommandHandler;
import ru.almaz.server.handler.MainHandler;
import ru.almaz.server.service.LoginService;
import ru.almaz.server.storage.UserStorage;

public class MainHandlerCreator {
    public static MainHandler create() {
        LoginService loginService = new LoginService(new UserStorage());
        return new MainHandler(new ClientCommandHandler(loginService), loginService);
    }
}
