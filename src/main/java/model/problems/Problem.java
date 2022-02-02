package model.problems;

import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import model.utils.AlgorithmResults;

import java.util.List;

public interface Problem {
    List<Integer> makeOneIndividual();
    Double fitness(List<Integer> individual);
    List<Integer> mutate(List<Integer> individual);
    Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2);

    String nameForFaces();
    String[] nameOfFxmlFiles();

    void visualize(Canvas canvas, AlgorithmResults data);
}
