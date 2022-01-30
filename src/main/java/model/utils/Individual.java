package model.utils;

import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;

import java.util.List;

@Getter
@Setter
public class Individual<T extends List<Integer>> {
    private T data;
    private Double fitness;

    public Individual(T data, Problem problem) {
        this.data = data;
        this.fitness = problem.fitness(data);
    }
}
