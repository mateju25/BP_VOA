package model.utils;

import lombok.Getter;
import model.problems.KnapsackProblem;
import model.problems.Problem;

import java.util.List;

@Getter
public class AlgorithmResults {
    private List<Integer> bestIndividualInGen;
    private Double bestFitnessInGen;
    private Double averageFitnessInGen;
    private Double worstFitnessInGen;
    private List<Integer> bestIndividual;
    private Double bestFitness;

    public AlgorithmResults(Problem problem, List<Integer> bestIndividualInGen, Double averageFitnessInGen, Double worstFitnessInGen, List<Integer> bestIndividual) {
        this.bestIndividualInGen = bestIndividualInGen;
        this.bestFitnessInGen = problem.fitness(bestIndividualInGen);
        this.averageFitnessInGen = averageFitnessInGen;
        this.worstFitnessInGen = worstFitnessInGen;
        this.bestIndividual = bestIndividual;
        this.bestFitness = problem.fitness(bestIndividual);
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
