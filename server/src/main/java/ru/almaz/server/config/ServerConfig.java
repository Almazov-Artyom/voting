package ru.almaz.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import ru.almaz.server.Server;
import ru.almaz.server.factory.HandlerFactory;
import ru.almaz.server.handler.MainHandler;

@Configuration
@ComponentScan(basePackages = "ru.almaz.server")
@PropertySource("classpath:application.properties")
public class ServerConfig {

    @Bean
    public Server server(HandlerFactory handlerFactory) {
        return new Server(handlerFactory);
    }

}
