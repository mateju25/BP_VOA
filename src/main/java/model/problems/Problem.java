package model.problems;

import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import model.algorithms.AntColonySystemAlgorithm;
import model.utils.AlgorithmResults;

import java.util.List;
import java.util.Map;

public interface Problem {
    void init(Map<String, String> parameters);
    List<Integer> makeOneIndividual();
    Double fitness(List<Integer> individual);
    List<Integer> mutate(List<Integer> individual);
    Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2);

    String nameForFaces();
    String[] nameOfFxmlFiles();

    void visualize(Canvas canvas, AlgorithmResults data);

    //ACS algorithm
    List<List<Double>> initPheromoneMatrix();
    List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs);
    Double getHeuristicValue(Integer from, Integer to);
}
