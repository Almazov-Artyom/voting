package ru.almaz.server.service.session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

import java.util.List;

class VoteCreateSessionTest {

    private Topic topic;
    private VoteCreateSession voteCreateSession;

    @BeforeEach
    void setUp() {
        topic = new Topic("TestTopic");
        voteCreateSession = new VoteCreateSession(topic);
    }

    @Test
    void nextStep() {
        assertEquals(0, voteCreateSession.getStep());

        voteCreateSession.nextStep();

        assertEquals(1, voteCreateSession.getStep());
    }

    @Test
    void canAddOption_CanMore() {
        voteCreateSession.setOptionsCount(3);

        voteCreateSession.addAnswerOptionToVote("Option 1");

        assertTrue(voteCreateSession.canAddOption());
    }

    @Test
    void canAddOption_CanNotMore() {
        voteCreateSession.setOptionsCount(1);

        voteCreateSession.addAnswerOptionToVote("Option 1");

        assertFalse(voteCreateSession.canAddOption());
    }

    @Test
    void setVoteName() {
        voteCreateSession.setVoteName("TestVote");

        assertEquals("TestVote", voteCreateSession.getVote().getName());
    }

    @Test
    void setVoteUserCreator() {
        voteCreateSession.setVoteUserCreator("User");

        assertEquals("User", voteCreateSession.getVote().getUserCreator());
    }

    @Test
    void setVoteDescription() {
        voteCreateSession.setVoteDescription("TestDescription");

        assertEquals("TestDescription", voteCreateSession.getVote().getDescription());
    }

    @Test
    void addAnswerOptionToVote() {
        voteCreateSession.addAnswerOptionToVote("Option 1");

        assertEquals(1, voteCreateSession.getAnswerOptionCount());
    }

}
