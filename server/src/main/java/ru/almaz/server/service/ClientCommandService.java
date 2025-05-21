package ru.almaz.server.service;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.almaz.server.controller.command.CommandController;


import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCommandService {
    @Value("${client.template.command.login}")
    private String loginTemplateCommand;

    private final Map<String, CommandController> commandControllers = new HashMap<>();

    private final LoginService loginService;

    public void registerCommandController(CommandController commandController) {
        commandControllers.put(commandController.getTemplateCommand(),commandController);
    }

    public void processingCommand(ChannelHandlerContext ctx, String msg) {
        if (!loginService.isLoggedIn(ctx.channel())) {
            if (msg.matches(loginTemplateCommand)) {
                loginService.login(ctx, msg);
            } else {
                ctx.writeAndFlush("Вы не авторизованы!\n");
            }
            return;
        }

        for (var entry : commandControllers.entrySet()) {
            if (msg.matches(entry.getKey())) {
                entry.getValue().accept(ctx, msg);
                return;
            }
        }
        ctx.writeAndFlush("Неверная команда\n");
        log.warn("#{}: Неверная команда - {}", ctx.channel().id(), msg);
    }
}
