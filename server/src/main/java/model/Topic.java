package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Topic {
    private String name;

    private List<Vote> votes;

    public Topic(String name) {
        this.name = name;
        this.votes = new ArrayList<>();
    }

}
