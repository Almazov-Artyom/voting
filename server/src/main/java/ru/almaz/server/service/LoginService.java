package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.almaz.server.storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class LoginService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final UserStorage userStorage;

    public void logout(Channel channel) {
        userStorage.deleteUser(channel);
    }

    public boolean isLoggedIn(Channel channel) {
        return userStorage.isUserExistByChannel(channel);
    }

    public void login(ChannelHandlerContext ctx, String msg) {
        String username = msg.substring("login -u=".length()).trim();

        if (!userStorage.isUserExistByUsername(username)) {
            userStorage.saveUser(ctx.channel(), username);
            ctx.writeAndFlush("Вы вошли под именем: " + username + "\n");
            logger.info("#" + ctx.channel().id() + ": Пользователь вошел под именем: " + username);
        } else {
            ctx.writeAndFlush("Пользователь с таким именем уже вошел\n");
            logger.warn("#" + ctx.channel().id() + ": Пользователь: " + username + " уже вошел");
        }

    }
}
