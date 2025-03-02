package ru.almaz.server.storage;

import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UserStorageTest {

    private UserStorage userStorage;

    private Channel channel;

    private String username;

    @BeforeEach
    void setUp() {
        userStorage = new UserStorage();
        channel = mock(Channel.class);
        username = "testUser";
    }

    @Test
    void saveUser() {

        userStorage.saveUser(channel, username);

        assertTrue(userStorage.isUserExistByChannel(channel));

        assertEquals(username, userStorage.findUserByChannel(channel));
    }

    @Test
    void deleteUser() {
        userStorage.saveUser(channel, username);

        userStorage.deleteUser(channel);

        assertFalse(userStorage.isUserExistByChannel(channel));
    }

    @Test
    void isUserExistByChannel_ExistingUser() {

        userStorage.saveUser(channel, username);

        assertTrue(userStorage.isUserExistByChannel(channel));
    }

    @Test
    void isUserExistByChannel_NonExistingUser() {
        assertFalse(userStorage.isUserExistByChannel(channel));
    }

    @Test
    void isUserExistByUsername_ExistingUser() {
        userStorage.saveUser(channel, username);

        assertTrue(userStorage.isUserExistByUsername(username));
    }

    @Test
    void isUserExistByUsername_NonExistingUser() {
        assertFalse(userStorage.isUserExistByUsername("nonExistentUser"));
    }

    @Test
    void findUserByChannel_ExistingUser() {
        userStorage.saveUser(channel, username);

        assertEquals(username, userStorage.findUserByChannel(channel));
    }

    @Test
    void findUserByChannel_NonExistingUser() {
        assertNull(userStorage.findUserByChannel(channel));
    }
}
