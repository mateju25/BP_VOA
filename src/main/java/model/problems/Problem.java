package model.problems;

import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import model.algorithms.AntColonySystemAlgorithm;
import model.utils.AlgorithmResults;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Problem {
    void init(Map<String, String> parameters);
    List<Integer> makeOneIndividual();
    Double fitness(List<Integer> individual);
    List<Integer> mutate(List<Integer> individual);
    Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2);

    String nameForFaces();
    String nameOfFxmlFiles();

    void visualize(Canvas canvas, AlgorithmResults data);

    default List<Integer> presetProblems() {
        return IntStream.rangeClosed(0, 2).boxed().collect(Collectors.toList());
    };
    void setPreset(Integer number);

    //ABC algorithm
    List<Integer> localSearch(List<Integer> individual, Double probChange);

    //ACS algorithm
    List<List<Double>> initPheromoneMatrix();
    List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs);
    Double getHeuristicValue(Integer from, Integer to);
}
