package ru.almaz.server.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Topic {
    private String name;

    private final List<Vote> votes;

    public Topic(String name) {
        this.name = name;
        this.votes = new ArrayList<>();
    }

}
