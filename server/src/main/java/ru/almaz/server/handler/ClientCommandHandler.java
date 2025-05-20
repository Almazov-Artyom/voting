package ru.almaz.server.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.almaz.server.manager.VoteManager;
import ru.almaz.server.service.LoginService;
import ru.almaz.server.service.TopicService;
import ru.almaz.server.service.VoteService;
import ru.almaz.server.storage.TopicStorage;
import ru.almaz.server.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ClientCommandHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClientCommandHandler.class);

    private final Map<String, BiConsumer<ChannelHandlerContext, String>> commands = new HashMap<>();

    private final LoginService loginService;

    private final TopicService topicService;

    private final VoteService voteService;

    public ClientCommandHandler(LoginService loginService, TopicService topicService, VoteService voteService) {
        this.loginService = loginService;
        this.topicService = topicService;
        this.voteService = voteService;

        commands.put("^create topic -n=.+$", topicService::createTopic);
        commands.put("^view$", topicService::view);
        commands.put("^view -t=([\\s\\S]+)$", topicService::viewPrefixT);
        commands.put("^create vote -t=.+$", voteService::startCreateVote);
        commands.put("^view -t=([^\\-]+) -v=(.+)$", voteService::view);
        commands.put("^vote -t=([^\\-]+) -v=(.+)$", voteService::startVote);
        commands.put("^delete -t=([^\\-]+) -v=(.+)$", voteService::deleteVote);
    }

    public void handleCommand(ChannelHandlerContext ctx, String msg) {
        if (!loginService.isLoggedIn(ctx.channel())) {
            if (msg.matches("^login -u=.+$")) {
                loginService.login(ctx, msg);
            } else {
                ctx.writeAndFlush("Вы не авторизованы!\n");
            }
            return;
        }

        for (var entry : commands.entrySet()) {
            if (msg.matches(entry.getKey())) {
                entry.getValue().accept(ctx, msg);
                return;
            }
        }
        ctx.writeAndFlush("Неверная команда\n");
        logger.warn("#" + ctx.channel().id() + ": Неверная команда - " + msg);
    }

}
