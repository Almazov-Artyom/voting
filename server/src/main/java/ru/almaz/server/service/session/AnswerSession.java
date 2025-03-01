package ru.almaz.server.service.session;

import ru.almaz.server.model.Vote;

public class AnswerSession {
    private final Vote vote;

    public AnswerSession(Vote vote) {
        this.vote = vote;
    }

    public void toAnswer(int numberAnswer, String username) {
        Vote.AnswerOption answerOption = vote.getAnswerOptions().get(numberAnswer - 1);
        answerOption.setCountUsers(answerOption.getCountUsers() + 1);
        vote.getAnswerUsers().add(username);
    }

    public int getCountAnswers() {
        return vote.getAnswerOptions().size();
    }
}
