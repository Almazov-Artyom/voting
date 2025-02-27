package ru.almaz.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Vote {
    private String name="";

    private String description="";

    private List<AnswerOption> answerOptions = new ArrayList<>();

    private String username="";

    public static class AnswerOption {
        private String answer;

        private long countUsers;

        public AnswerOption(String answer) {
            this.answer = answer;
        }
    }

}
