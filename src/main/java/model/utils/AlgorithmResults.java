package model.utils;

import lombok.Getter;
import model.problems.Problem;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AlgorithmResults {
    private final List<Integer> bestIndividualInGen;
    private final Double bestFitnessInGen;
    private final Double averageFitnessInGen;
    private final List<Integer> bestIndividual;
    private final Double bestFitness;
    private final Integer actualGeneration;
    private final Integer maxGeneration;


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
