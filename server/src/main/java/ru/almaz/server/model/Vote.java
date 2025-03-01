package ru.almaz.server.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Vote {
    private String name = "";

    private String description = "";

    private List<AnswerOption> answerOptions = new ArrayList<>();

    private String userCreator = "";

    private List<String> answerUsers = new ArrayList<>();

    @Getter
    @Setter
    public static class AnswerOption {
        private String answer;

        private long countUsers;

        public AnswerOption(String answer) {
            this.answer = answer;
        }
    }

}
