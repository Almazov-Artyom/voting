package ru.almaz.server.controller;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.almaz.server.service.ClientCommandService;

import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

public interface CommandController {
    void accept(ChannelHandlerContext ctx, String message);

    String getTemplateCommand();

    @Autowired
    default void register(ClientCommandService clientCommandService) {
        clientCommandService.registerCommandController(this);
    }
}
