package ru.almaz.server.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

public class VoteManagerTest {
    private VoteManager voteManager;

    private Topic topic;

    @BeforeEach
    public void setUp() {
        voteManager = new VoteManager();
        String voteName = "TestVote";

        topic = new Topic("");
        Vote vote = new Vote();
        vote.setName(voteName);
        topic.getVotes().add(vote);
    }

    @Test
    public void isVoteInTopicExists_VoteNotInTopic() {
        boolean result = voteManager.isVoteInTopicExists(topic, "VoteTest");
        Assertions.assertFalse(result);
    }

    @Test
    public void isVoteInTopicExists_VoteInTopic() {

        boolean result = voteManager.isVoteInTopicExists(topic, "TestVote");
        Assertions.assertTrue(result);
    }
}
