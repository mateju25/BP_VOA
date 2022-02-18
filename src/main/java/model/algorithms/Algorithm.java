package model.algorithms;

import controllers.base.BaseController;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface Algorithm {
    void init(Map<String, String> parameters);

    void initFirstGeneration();
    AlgorithmResults nextGeneration();
    void resetAlgorithm();
    String nameForFaces();
    String[] nameOfFxmlFiles();

    void setProblem(Problem chosenProblem);
    Problem getProblem();

    // this code was inspired by https://github.com/dwdyer/watchmaker/blob/master/framework/src/java/main/org/uncommons/watchmaker/framework/selection/RouletteWheelSelection.java
    static int getCumulativeFitnessesIndex(List<Double> generation) {
        double[] cumulativeFitnesses = new double[generation.size()];
        cumulativeFitnesses[0] = generation.get(0);
        for (int i = 1; i < generation.size(); i++)
        {
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + generation.get(i);
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
