package ru.almaz.server.controller.command;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.almaz.server.service.ClientCommandService;

public interface CommandController {
    void accept(ChannelHandlerContext ctx, String message);

    String getTemplateCommand();

    @Autowired
    default void register(ClientCommandService clientCommandService) {
        clientCommandService.registerCommandController(this);
    }
}
