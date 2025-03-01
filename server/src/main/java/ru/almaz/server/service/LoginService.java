package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import ru.almaz.server.storage.UserStorage;


public class LoginService {

    public static void logout(Channel channel) {
        UserStorage.deleteUser(channel);
    }

    public static boolean isLoggedIn(Channel channel) {
        return UserStorage.isUserExistByChannel(channel);
    }

    public static void loginCommand(ChannelHandlerContext ctx, String msg) {
        String username = msg.substring("login -u=".length());

        if (!UserStorage.isUserExistByUsername(username)) {
            UserStorage.saveUser(ctx.channel(), username);
            ctx.writeAndFlush("Вы вошли под именем: " + username + "\n");
        } else
            ctx.writeAndFlush("Пользователь с таким именем уже вошел\n");
    }
}
