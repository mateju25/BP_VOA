package model.algorithms;

import controllers.base.BaseController;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Interface for all algorithm implemented in application.
 */
public interface Algorithm {
    /**
     * Initializes algorithm with parameters.
     * @param parameters parameters from input fields.
     */
    void init(Map<String, String> parameters);

    /**
     * Creates first generation.
     */
    void initFirstGeneration();

    /**
     * Runs one iteration of algorithm.
     * @return results after one run.
     */
    AlgorithmResults nextGeneration();

    /**
     * Resets algorithm to the first generation.
     */
    void resetAlgorithm();

    /**
     * @return message that will be displayed in simulation.
     */
    String nameForFaces();

    /**
     * @return all components that will controller need.
     */
    String[] nameOfFxmlFiles();

    /**
     * Connects problem to the algorithm
     * @param chosenProblem
     */
    void setProblem(Problem chosenProblem);

    /**
     * This code was inspired by https://github.com/dwdyer/watchmaker/blob/master/framework/src/java/main/org/uncommons/watchmaker/framework/selection/RouletteWheelSelection.java
     * @param doubles double values of chances
     * @return index of random proportionate selection.
     */
    static int getCumulativeFitnessesIndex(List<Double> doubles) {
        double[] cumulativeFitnesses = new double[doubles.size()];
        cumulativeFitnesses[0] = doubles.get(0);
        for (int i = 1; i < doubles.size(); i++)
        {
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + doubles.get(i);
        }
        double randomFitness = BaseController.randomGenerator.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
        int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
        if (index < 0)
        {
            index = Math.abs(index + 1);
        }
        return index;
    }
}
