package model.algorithms;

import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.List;

public interface Algorithm {
    void initFirstGeneration();
    AlgorithmResults nextGeneration();
    void resetAlgorithm();
    String nameForFaces();
    String[] nameOfFxmlFiles();

    void setProblem(Problem chosenProblem);
    Problem getProblem();

    // this code was inspired by https://github.com/dwdyer/watchmaker/blob/master/framework/src/java/main/org/uncommons/watchmaker/framework/selection/RouletteWheelSelection.java
    static double[] makeCumulativeFitnesses(Problem problem, List<List<Integer>> generation) {
        double[] cumulativeFitnesses = new double[generation.size()];
        cumulativeFitnesses[0] = problem.fitness(generation.get(0));
        for (int i = 1; i < generation.size(); i++)
        {
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + problem.fitness(generation.get(i));
        }
        return cumulativeFitnesses;
    }
}
