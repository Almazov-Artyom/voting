package ru.almaz.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.almaz.server.model.Topic;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TopicStorageTest {
    private TopicStorage topicStorage;

    @BeforeEach
    void setUp() {
        topicStorage = new TopicStorage();
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
    void testIsTopicExists_NonExistingTopic() {
        String topicName = "TestTopic";

        assertFalse(topicStorage.isTopicExists(topicName));
    }

    @Test
    void testFindTopicByName_ExistingTopic() {
        // Сохраняем тему
        topicStorage.saveTopic(topic);

        // Ищем тему по имени
        Optional<Topic> foundTopic = topicStorage.findTopicByName(topicName);

        // Проверяем, что тема найдена
        assertTrue(foundTopic.isPresent());
        assertEquals(topicName, foundTopic.get().getName());
    }

    @Test
    void testFindTopicByName_NonExistingTopic() {
        // Ищем несуществующую тему
        Optional<Topic> foundTopic = topicStorage.findTopicByName("nonExistingTopic");

        // Проверяем, что тема не найдена
        assertFalse(foundTopic.isPresent());
    }

    @Test
    void testFindAllTopics() {
        // Добавляем несколько тем
        Topic topic1 = new Topic("topic1");
        Topic topic2 = new Topic("topic2");
        topicStorage.saveTopic(topic1);
        topicStorage.saveTopic(topic2);

        // Проверяем, что метод findAllTopics возвращает все сохраненные темы
        List<Topic> allTopics = topicStorage.findAllTopics();
        assertEquals(2, allTopics.size());
        assertTrue(allTopics.contains(topic1));
        assertTrue(allTopics.contains(topic2));
    }

    @Test
    void testSetTopics() {
        // Сохраняем несколько тем
        Topic topic1 = new Topic("topic1");
        Topic topic2 = new Topic("topic2");
        List<Topic> newTopics = List.of(topic1, topic2);

        // Устанавливаем новые темы
        topicStorage.setTopics(newTopics);

        // Проверяем, что темы были обновлены
        List<Topic> allTopics = topicStorage.findAllTopics();
        assertEquals(2, allTopics.size());
        assertTrue(allTopics.contains(topic1));
        assertTrue(allTopics.contains(topic2));
    }
}
