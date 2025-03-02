package ru.almaz.server.handler;

import lombok.RequiredArgsConstructor;
import ru.almaz.server.service.FileService;
import ru.almaz.server.storage.TopicStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class ServerCommandHandler {
    private final Map<String, Consumer<String>> commands;

    public ServerCommandHandler() {
        FileService fileService = new FileService(new TopicStorage());
        commands = new HashMap<>();
        commands.put("^load .+$", fileService::loadFile);
        commands.put("^save .+$", fileService::saveFile);
    }

    public void handleCommand(String command) {
        for (var entry : commands.entrySet()) {
            if (command.matches(entry.getKey())) {
                entry.getValue().accept(command);
                return;
            }
        }
        System.out.println("Неверная команда");
    }
}
