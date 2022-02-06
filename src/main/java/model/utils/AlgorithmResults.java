package model.utils;

import lombok.Getter;
import model.problems.KnapsackProblem;
import model.problems.Problem;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AlgorithmResults {
    private final List<Integer> bestIndividualInGen;
    private final Double bestFitnessInGen;
    private final Double averageFitnessInGen;
    private final Double worstFitnessInGen;
    private final List<Integer> bestIndividual;
    private final Double bestFitness;
    private final Integer actualGeneration;


    public AlgorithmResults(Problem problem, List<Integer> bestIndividualInGen, Double averageFitnessInGen, Double worstFitnessInGen, List<Integer> bestIndividual, Integer actualGeneration) {
        this.bestIndividualInGen = new ArrayList<>(bestIndividualInGen);
        this.bestFitnessInGen = problem.fitness(bestIndividualInGen);
        this.averageFitnessInGen = averageFitnessInGen;
        this.worstFitnessInGen = worstFitnessInGen;
        this.bestIndividual =  new ArrayList<>(bestIndividual);
        this.bestFitness = problem.fitness(bestIndividual);
        this.actualGeneration = actualGeneration;
    }

    public String toString(Problem problem) {
        return "AlgorithmResults{" +
                "bestIndividualInGen=" + ((KnapsackProblem)problem).sumOfItems(bestIndividualInGen) +
                ", bestFitnessInGen=" + bestFitnessInGen +
                ", averageFitnessInGen=" + averageFitnessInGen +
                ", worstFitnessInGen=" + worstFitnessInGen +
                ", bestIndividual=" + ((KnapsackProblem)problem).sumOfItems(bestIndividual) +
                ", bestFitness=" + bestFitness +
                '}';
    }
}
