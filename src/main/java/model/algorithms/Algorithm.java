package model.algorithms;

import model.problems.Problem;
import model.utils.AlgorithmResults;

public interface Algorithm {
    void initFirstGeneration();
    AlgorithmResults nextGeneration();
    void resetAlgorithm();
    String nameForFaces();
    String[] nameOfFxmlFiles();

    void setProblem(Problem chosedProblem);
    Problem getProblem();
}
