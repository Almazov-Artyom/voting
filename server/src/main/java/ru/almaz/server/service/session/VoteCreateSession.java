package ru.almaz.server.service.session;

import lombok.Getter;
import lombok.Setter;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;

@Setter
@Getter
public class VoteCreateSession {
    private int step;

    private int optionsCount;

    private Topic topic;

    private final Vote vote = new Vote();

    public VoteCreateSession(Topic topic) {
        this.topic = topic;
    }

    public void nextStep() {
        step++;
    }

    public boolean canAddOption() {
        return vote.getAnswerOptions().size() < optionsCount;
    }

    public void setVoteName(String voteName) {
        vote.setName(voteName);
    }
    public void setVoteUserCreator(String userCreator) {
        vote.setUserCreator(userCreator);
    }

    public void setVoteDescription(String description) {
        vote.setDescription(description);
    }

    public void addAnswerOptionToVote(String answerOption) {
        vote.getAnswerOptions().add(new Vote.AnswerOption(answerOption));
    }

    public void addVoteInTopic() {
        topic.getVotes().add(vote);
    }

    public int getAnswerOptionCount() {
        return vote.getAnswerOptions().size();
    }

}
