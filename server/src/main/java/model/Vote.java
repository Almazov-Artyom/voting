package model;

import java.util.List;

public class Vote {
    private String name;

    private String description;

    private List<AnswerOption> answerOptions;

    private String username;

    private static class AnswerOption {
        private String answer;

        private long countUsers;
    }

}
