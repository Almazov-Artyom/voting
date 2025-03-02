package ru.almaz.server.service.session;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.almaz.server.model.Vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnswerSessionTest {
    private AnswerSession answerSession;

    private Vote vote;

    @BeforeEach
    public void setUp() {
        vote = new Vote();
        vote.getAnswerOptions().add(new Vote.AnswerOption("TestAnswerOption1"));
        vote.getAnswerOptions().add(new Vote.AnswerOption("TestAnswerOption2"));
        answerSession = new AnswerSession(vote);
    }

    @Test
    public void toAnswer() {
        answerSession.toAnswer(1, "User");
        assertEquals(1, vote.getAnswerOptions().get(0).getCountUsers());
        assertTrue(vote.getAnswerUsers().contains("User"));

    }

    @Test
    public void getCountAnswers() {
        assertEquals(2, answerSession.getCountAnswers());
    }
}
