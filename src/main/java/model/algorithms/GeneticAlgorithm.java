package model.algorithms;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class GeneticAlgorithm implements Algorithm {
    private List<List<Integer>> generation = new ArrayList<>();
    private List<Integer> bestIndividual = new ArrayList<>();
    private Integer actualGeneration = 0;
    private Integer numOfIndividuals;
    private Integer numOfGenerations;
    private Double percentageRoulette;
    private Double percentageTournament;
    private Integer sizeTournament;
    private Double percentageElitism;
    private Double percentageMutation;

    public void setAlgorithm(Integer numOfIndividuals, Integer numOfGenerations, Double percentageRoulette, Double percentageTournament, Integer sizeTournament, Double percentageElitism, Double percentageMutation) {
        this.numOfIndividuals = numOfIndividuals;
        this.numOfGenerations = numOfGenerations;
        this.percentageRoulette = percentageRoulette;
        this.percentageTournament = percentageTournament;
        this.sizeTournament = sizeTournament;
        this.percentageElitism = percentageElitism;
        this.percentageMutation = percentageMutation;
    }

    public void initFirstGeneration(Problem problem) {
        for (int i = 0; i < this.numOfIndividuals; i++) {
            generation.add(problem.makeOneIndividual());
        }
    }

    public List<List<Integer>> rouletteSelection(Problem problem)
    {
        var rndm = new Random();
        double[] cumulativeFitnesses = new double[generation.size()];
        cumulativeFitnesses[0] = problem.fitness(generation.get(0));
        for (int i = 1; i < generation.size(); i++)
        {
            double fitness =  problem.fitness(generation.get(i));
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + fitness;
        }

        // this code was inspired by https://github.com/dwdyer/watchmaker/blob/master/framework/src/java/main/org/uncommons/watchmaker/framework/selection/RouletteWheelSelection.java
        var parents = new ArrayList<List<Integer>>(2);
        for (int i = 0; i < 2; i++)
        {
            double randomFitness = rndm.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
            int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
            if (index < 0)
            {
                index = Math.abs(index + 1);
            }
            parents.add(generation.get(index));
        }
        var children = new ArrayList<List<Integer>>(2);
        var resCrossover = problem.simpleCrossover(children.get(0), children.get(1));
        children.add(resCrossover.getKey());
        children.add(resCrossover.getValue());
        return children;
    }

    public AlgorithmResults nextGeneration(Problem problem) {
        if (actualGeneration < numOfGenerations) {
            generation = generation.stream().sorted(Comparator.comparing(problem::fitness)).collect(Collectors.toList());
            List<List<Integer>> newGeneration = new ArrayList<>();
            for (int i = 0; i < numOfIndividuals * percentageElitism; i++) {
                newGeneration.add(generation.get(i));
            }
            // todo: mozno tu nastane chyba bo sa bude pridatavat po dvojiciach
            for (int i = 0; i < numOfIndividuals * percentageRoulette; i++) {
                newGeneration.addAll(rouletteSelection(problem));
            }
            while (newGeneration.size() < generation.size()) {
                newGeneration.add(problem.makeOneIndividual());
            }
            var rndm = new Random();
            for (int i = 0; i < newGeneration.size(); i++) {
                if (rndm.nextDouble() < this.percentageMutation)
                    newGeneration.set(i, problem.mutate(newGeneration.get(i)));
            }
            if (problem.fitness(newGeneration.get(0)) < problem.fitness(bestIndividual))
                bestIndividual = newGeneration.get(0);
            this.generation = newGeneration;
            var avgFitness = generation.stream().mapToDouble(problem::fitness).average().getAsDouble();
            return new AlgorithmResults(problem, generation.get(0), avgFitness, problem.fitness(generation.get(generation.size()-1)), bestIndividual);
        } else
            return null;
    }

    @Override
    public String nameForFaces() {
        return "Genetic Algorithm";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "GAPage.fxml";
        return arr;
    }
}
