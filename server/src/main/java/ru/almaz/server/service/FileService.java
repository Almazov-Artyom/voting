package ru.almaz.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class FileService {

    private final TopicStorage topicStorage;

    public void loadFile(String command) {
        String filename = command.substring("load ".length());
        File file = new File(filename);

        if (!file.exists()) {
            System.out.printf("Файл %s не найден%n", filename);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        List<Topic> topics = null;
        try {
            topics = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Topic.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        topicStorage.setTopics(topics);
    }

    public void saveFile(String command) {
        String filename = command.substring("save ".length());
        List<Topic> topics = topicStorage.findAllTopics();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(filename), topics);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
