package ru.almaz.server.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.almaz.server.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TopicStorageTest {
    private TopicStorage topicStorage;

    @BeforeEach
    void setUp() {
        topicStorage = new TopicStorage();
        topicStorage.setTopics(new ArrayList<>());
    }

    @Test
    void saveTopic() {
        String topicName = "TestTopic";

        Topic topic = new Topic();
        topic.setName(topicName);

        topicStorage.saveTopic(topic);

        assertTrue(topicStorage.isTopicExists(topicName));

        Optional<Topic> foundTopic = topicStorage.findTopicByName(topicName);
        assertTrue(foundTopic.isPresent());
        assertEquals(topicName, foundTopic.get().getName());
    }

    @Test
    void isTopicExists_ExistingTopic() {
        String topicName = "TestTopic";

        Topic topic = new Topic();
        topic.setName(topicName);
        topicStorage.saveTopic(topic);

        assertTrue(topicStorage.isTopicExists(topicName));
    }

    @Test
    void isTopicExists_NonExistingTopic() {
        String topicName = "NonExistingTopic";
        assertFalse(topicStorage.isTopicExists(topicName));
    }

    @Test
    void findTopicByName_ExistingTopic() {
        String topicName = "TestTopic";

        Topic topic = new Topic();
        topic.setName(topicName);
        topicStorage.saveTopic(topic);

        Optional<Topic> foundTopic = topicStorage.findTopicByName(topicName);

        assertTrue(foundTopic.isPresent());
        assertEquals(topicName, foundTopic.get().getName());
    }

    @Test
    void findTopicByName_NonExistingTopic() {
        Optional<Topic> foundTopic = topicStorage.findTopicByName("nonExistingTopic");

        assertFalse(foundTopic.isPresent());
    }

    @Test
    void findAllTopics() {
        Topic topic1 = new Topic("topic1");
        Topic topic2 = new Topic("topic2");
        topicStorage.saveTopic(topic1);
        topicStorage.saveTopic(topic2);

        List<Topic> allTopics = topicStorage.findAllTopics();
        assertEquals(2, allTopics.size());
        assertTrue(allTopics.contains(topic1));
        assertTrue(allTopics.contains(topic2));
    }

    @Test
    void setTopics() {
        Topic topic1 = new Topic("topic1");
        Topic topic2 = new Topic("topic2");
        List<Topic> newTopics = List.of(topic1, topic2);

        topicStorage.setTopics(newTopics);

        List<Topic> allTopics = topicStorage.findAllTopics();
        assertEquals(2, allTopics.size());
        assertTrue(allTopics.contains(topic1));
        assertTrue(allTopics.contains(topic2));
    }
}
