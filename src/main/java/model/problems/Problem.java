package model.problems;

import javafx.util.Pair;

import java.util.List;

public interface Problem {
    List<Integer> makeOneIndividual();
    Double fitness(List<Integer> individual);
    List<Integer> mutate(List<Integer> individual);
    Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2);

    String nameForFaces();
    String[] nameOfFxmlFiles();
}
