package ru.almaz.server.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Vote {
    private String name;

    private String description;

    private final List<AnswerOption> answerOptions;

    private String userCreator;

    private final List<String> answerUsers;

    public Vote() {
        name = "";
        description = "";
        answerOptions = new ArrayList<>();
        userCreator = "";
        answerUsers = new ArrayList<>();

    }

    @Getter
    @Setter
    public static class AnswerOption {
        private String answer;

        private int countUsers;

        public AnswerOption(String answer) {
            this.answer = answer;
            countUsers = 0;
        }
    }

}
