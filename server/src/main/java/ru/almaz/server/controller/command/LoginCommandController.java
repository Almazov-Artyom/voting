package ru.almaz.server.controller.command;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.almaz.server.service.LoginService;

@Component
@RequiredArgsConstructor
public class LoginCommandController implements CommandController {

    @Value("${client.template.command.login}")
    private String loginTemplate;

    private final LoginService loginService;

    @Override
    public void accept(ChannelHandlerContext channelHandlerContext, String message) {
        loginService.login(channelHandlerContext, message);
    }

    @Override
    public String getTemplateCommand() {
        return loginTemplate;
    }
}
