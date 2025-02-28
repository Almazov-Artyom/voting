package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.storage.UserStorage;

@RequiredArgsConstructor
public class LoginService {
    private final UserStorage userStorage = new UserStorage();


    public void logout(Channel channel) {
        userStorage.deleteUser(channel);
    }

    public boolean isLoggedIn(Channel channel) {
        return userStorage.isUserExistByChannel(channel);
    }

    public void loginCommand(ChannelHandlerContext ctx, String msg) {
        String username = msg.substring("login -u=".length());

        if(!userStorage.isUserExistByUsername(username)) {
            userStorage.saveUser(ctx.channel(), username);
            ctx.writeAndFlush("Вы вошли под именем: "+username);
        } else
            ctx.writeAndFlush("Пользователь с таким именем уже вошел");
    }
}
