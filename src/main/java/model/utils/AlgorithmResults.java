package model.utils;

import lombok.Getter;
import model.problems.Problem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds data about each iteration of algorithm
 */
@Getter
public class AlgorithmResults {
    private final List<Integer> bestIndividualInGen;
    private final Double bestFitnessInGen;
    private final Double averageFitnessInGen;
    private final List<Integer> bestIndividual;
    private final Double bestFitness;
    private final Integer actualGeneration;
    private final Integer maxGeneration;

    /**
     * Setups all data.
     * @param problem used problem.
     * @param bestIndividualInGen best individual in iteration.
     * @param averageFitnessInGen average fitness in iteration.
     * @param bestIndividual best individual so far.
     * @param actualGeneration number of current iteration.
     * @param maxGeneration maximum number of iterations.
     */
    public AlgorithmResults(Problem problem, List<Integer> bestIndividualInGen, Double averageFitnessInGen, List<Integer> bestIndividual, Integer actualGeneration, Integer maxGeneration) {
        this.bestIndividualInGen = new ArrayList<>(bestIndividualInGen);
        this.bestFitnessInGen = problem.fitness(bestIndividualInGen);
        this.averageFitnessInGen = averageFitnessInGen;
        this.bestIndividual = new ArrayList<>(bestIndividual);
        this.bestFitness = problem.fitness(bestIndividual);
        this.actualGeneration = actualGeneration;
        this.maxGeneration = maxGeneration;
    }
}
