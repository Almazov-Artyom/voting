package ru.almaz.server.storage;

import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TopicStorage {
    private static final List<Topic> topics = new ArrayList<>();

    public boolean isTopicExists(String topicName) {
        return topics.stream().anyMatch(topic -> topic.getName().equals(topicName));

    }

    public boolean isVoteInTopicExists(Topic topic, String voteName) {
        return topic.getVotes()
                .stream()
                .anyMatch(vote -> vote.getName().equals(voteName));
    }

    public void saveTopic(Topic topic) {
        topics.add(topic);
    }

    public Optional<Topic> findTopicByName(String topicName) {
        return topics.stream()
                .filter(topic -> topic.getName().equals(topicName))
                .findFirst();
    }
    public Optional<Vote> findVoteByTopic(Topic topic, String voteName) {
        return topic.getVotes()
                .stream()
                .filter(vote -> vote.getName().equals(voteName))
                .findFirst();
    }


    public List<Topic> findAllTopics() {
        return topics;
    }



}
