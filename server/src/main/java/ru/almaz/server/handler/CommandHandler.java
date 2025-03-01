package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import ru.almaz.server.service.LoginService;
import ru.almaz.server.service.TopicService;
import ru.almaz.server.service.VoteService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CommandHandler {
    private final Map<String, BiConsumer<ChannelHandlerContext, String>> commands = new HashMap<>();

    public CommandHandler() {
        commands.put("^create topic -n=.+$", TopicService::createTopic);
        commands.put("^view$", TopicService::view);
        commands.put("^view -t=([\\s\\S]+)$", TopicService::viewPrefixT);
        commands.put("^create vote -t=.+$", VoteService::startCreateVote);
        commands.put("^view -t=([^\\-]+) -v=(.+)$", VoteService::view);
        commands.put("^vote -t=([^\\-]+) -v=(.+)$", VoteService::startVote);
        commands.put("^delete -t=([^\\-]+) -v=(.+)$", VoteService::deleteVote);
    }

    public void handleCommand(ChannelHandlerContext ctx, String msg) {
        System.out.println("handleCommand" + msg);
        if (!LoginService.isLoggedIn(ctx.channel())) {
            if (msg.matches("^login -u=.+$")) {
                System.out.println("handleCommand: login");
                LoginService.loginCommand(ctx, msg);
            } else {
                ctx.writeAndFlush("Вы не авторизованы!\n");
            }
            return;
        }

        for (var entry : commands.entrySet()) {
            if (msg.matches(entry.getKey())) {
                entry.getValue().accept(ctx, msg);
                System.out.println(entry.getKey());
                return;
            }
        }
        ctx.writeAndFlush("Неверная команда\n");
    }

}
