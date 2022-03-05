package model.problems;

import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import model.algorithms.AntColonySystemAlgorithm;
import model.utils.AlgorithmResults;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Interface for all implemented problems in application
 */
public interface Problem {
    /**
     * Initializes problem with parameters.
     * @param parameters parameters from input fields.
     */
    void init(Map<String, String> parameters);

    /**
     * Regenerates problem
     */
    void regenerate();

    /**
     * Make one solution of the problem, used in GA, ABC
     * @return individual
     */
    List<Integer> makeOneIndividual();

    /**
     * @param individual solution of the problem
     * @return fitness value of solution
     */
    Double fitness(List<Integer> individual);

    /**
     * Changes individual, used in GA
     * @param individual solution of the problem
     * @return mutated individual
     */
    List<Integer> mutate(List<Integer> individual);

    /**
     * Creates two children by single point cross-overing parents, used in GA
     * @param parent1 one solution of the problem
     * @param parent2 second solution of the problem
     * @return pair of children
     */
    Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2);

    /**
     * Creates two children by double point cross-overing parents, used in GA
     * @param parent1 one solution of the problem
     * @param parent2 second solution of the problem
     * @return pair of children
     */
    Pair<List<Integer>, List<Integer>> doubleCrossover(List<Integer> parent1, List<Integer> parent2);

    /**
     * @return message that will be displayed in simulation.
     */
    String nameForFaces();
    /**
     * @return component that will controller need.
     */
    String nameOfFxmlFiles();

    /**
     * Visualize best solution to the provided canvas.
     * @param canvas visualization place
     * @param data algorithm results
     */
    void visualize(Canvas canvas, AlgorithmResults data);

    /**
     * @return list of preset problems
     */
    default List<Integer> presetProblems() {
        return IntStream.rangeClosed(0, 2).boxed().collect(Collectors.toList());
    }

    /**
     * Sets parameters.
     * @param number index of preset problem
     */
    void setPreset(Integer number);

    /**
     * Changes individual, used in ABC
     * @param individual one solution of the problem
     * @param probChange strength of the mutation
     * @return changed individual
     */
    List<Integer> localSearch(List<Integer> individual, Double probChange);

    /**
     * Initializes matrix of pheromones with dimension of the problem, used in ACS
     * @return matrix of pheromones
     */
    List<List<Double>> initPheromoneMatrix();

    /**
     * Make one solution of the problem, used in ACS
     * @param acs ACS algorithm instance
     * @return individual
     */
    List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs);

    /**
     * @param from start node
     * @param to to node
     * @return heuristic value of one line in graph
     */
    Double getHeuristicValue(Integer from, Integer to);

    /**
     * Generates edges that will have increased pheromone level
     * @param individual best individual in iteration
     * @return matrix of pheromones
     */
    List<List<Double>> generateEdges(List<Integer> individual);
}
