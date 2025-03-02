package ru.almaz.server.service;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.almaz.server.storage.UserStorage;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginServiceTest {
    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ctx.channel()).thenReturn(channel);
    }

    @Test
    public void login_UserNotExist() {
        String msg = "login -u=TestUser";

        when(userStorage.isUserExistByUsername("TestUser")).thenReturn(false); // Пользователь не существует

        loginService.login(ctx, msg);

        verify(userStorage).saveUser(channel, "TestUser");

        verify(ctx).writeAndFlush("Вы вошли под именем: TestUser\n");
    }

    @Test
    public void login_UserExist() {
        String msg = "login -u=TestUser";

        when(userStorage.isUserExistByUsername("TestUser")).thenReturn(true); // Пользователь не существует

        loginService.login(ctx, msg);

        verify(ctx).writeAndFlush("Пользователь с таким именем уже вошел\n");
    }

    @Test
    public void logout() {
        loginService.logout(channel);
        verify(userStorage).deleteUser(channel);
    }

    @Test
    public void isLoggedIn() {
        loginService.isLoggedIn(channel);
        verify(userStorage).isUserExistByChannel(channel);
    }

}
