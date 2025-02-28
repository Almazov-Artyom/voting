package ru.almaz.server.storage;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final Map<Channel,String> users = new HashMap<>();

    public void deleteUser(Channel channel) {
        users.remove(channel);
    }

    public void saveUser(Channel channel, String username){
        users.put(channel,username);
    }

    public boolean isUserExistByChannel(Channel channel){
        return users.containsKey(channel);
    }

    public boolean isUserExistByUsername(String username){
        return users.containsValue(username);
    }

    public String findUserByChannel(Channel channel){
        return users.get(channel);
    }

}
