package ru.almaz.server.storage;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final Map<Channel, String> users = new HashMap<>();

    public static void deleteUser(Channel channel) {
        users.remove(channel);
    }

    public static void saveUser(Channel channel, String username) {
        users.put(channel, username);
    }

    public static boolean isUserExistByChannel(Channel channel) {
        return users.containsKey(channel);
    }

    public static boolean isUserExistByUsername(String username) {
        return users.containsValue(username);
    }

    public static String findUserByChannel(Channel channel) {
        return users.get(channel);
    }

}
