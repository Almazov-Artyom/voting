package ru.almaz.server.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class TopicStorage {
    private static final List<Topic> topics = new ArrayList<>();

    public static boolean isTopicExists(String topicName) {
        return topics.stream().anyMatch(topic -> topic.getName().equals(topicName));

    }

    public static void saveTopic(Topic topic) {
        topics.add(topic);
    }

    public static Optional<Topic> findTopicByName(String topicName) {
        return topics.stream()
                .filter(topic -> topic.getName().equals(topicName))
                .findFirst();
    }

    public static List<Topic> findAllTopics() {
        return topics;
    }

    public static void saveTopicsToFile(String filename) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(filename), topics);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
