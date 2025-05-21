package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.almaz.server.storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
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
            log.info("#{}: Пользователь вошел под именем: {}", ctx.channel().id(), username);
        } else {
            ctx.writeAndFlush("Пользователь с таким именем уже вошел\n");
            log.warn("#{}: Пользователь: {} уже вошел", ctx.channel().id(), username);
        }

    }
}
