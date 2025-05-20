package ru.almaz.server.manager;

import org.springframework.stereotype.Component;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

import java.util.Optional;

@Component
public class VoteManager {

    public boolean isVoteInTopicExists(Topic topic, String voteName) {
        return topic.getVotes()
                .stream()
                .anyMatch(vote -> vote.getName().equals(voteName));
    }

    public Optional<Vote> findVoteByTopic(Topic topic, String voteName) {
        return topic.getVotes()
                .stream()
                .filter(vote -> vote.getName().equals(voteName))
                .findFirst();
    }
}
