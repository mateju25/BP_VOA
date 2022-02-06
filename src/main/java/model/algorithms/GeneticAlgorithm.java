package model.algorithms;

import controllers.base.BaseController;
import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class GeneticAlgorithm implements Algorithm {
    private List<List<Integer>> generation = new ArrayList<>();
    private List<Integer> bestIndividual = new ArrayList<>();
    private Problem problem;
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
        resetAlgorithm();
    }

    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
    }

    public void initFirstGeneration() {
        for (int i = 0; i < this.numOfIndividuals; i++) {
            generation.add(problem.makeOneIndividual());
        }
    }

    private List<List<Integer>> makeChildrenWithCrossover(List<List<Integer>> parents) {
        var children = new ArrayList<List<Integer>>(2);
        var resCrossover = problem.simpleCrossover(parents.get(0), parents.get(1));
        children.add(resCrossover.getKey());
        children.add(resCrossover.getValue());
        return children;
    }

    public List<List<Integer>> tournamentSelection() {
        var tournamentMembers = new ArrayList<List<Integer>>();
        BaseController.rndm.ints(0, generation.size()).limit(sizeTournament).forEach(index -> {
            tournamentMembers.add(generation.get(index));
        });

        var parents = tournamentMembers.stream().sorted(Comparator.comparing(problem::fitness)).limit(2).collect(Collectors.toList());

        return makeChildrenWithCrossover(parents);
    }

    public List<List<Integer>> rouletteSelection() {
        double[] cumulativeFitnesses = new double[generation.size()];
        cumulativeFitnesses[0] = problem.fitness(generation.get(0));
        for (int i = 1; i < generation.size(); i++)
        {
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + problem.fitness(generation.get(i));
        }

        // this code was inspired by https://github.com/dwdyer/watchmaker/blob/master/framework/src/java/main/org/uncommons/watchmaker/framework/selection/RouletteWheelSelection.java
        var parents = new ArrayList<List<Integer>>(2);
        for (int i = 0; i < 2; i++)
        {
            double randomFitness = BaseController.rndm.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
            int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
            if (index < 0)
            {
                index = Math.abs(index + 1);
            }
            parents.add(generation.get(index));
        }
        return makeChildrenWithCrossover(parents);
    }

    public AlgorithmResults nextGeneration() {
        if (actualGeneration < numOfGenerations) {
            generation = generation.stream().sorted(Comparator.comparing(problem::fitness)).collect(Collectors.toList());
            List<List<Integer>> newGeneration = new ArrayList<>();
            for (int i = 0; i < numOfIndividuals * percentageElitism; i++) {
                newGeneration.add(generation.get(i));
            }
            for (int i = 0; i < numOfIndividuals * percentageRoulette; i++) {
                newGeneration.add(rouletteSelection().get(0));
            }
            for (int i = 0; i < numOfIndividuals * percentageTournament; i++) {
                newGeneration.add(tournamentSelection().get(0));
            }
            while (newGeneration.size() < generation.size()) {
                newGeneration.add(problem.makeOneIndividual());
            }
            for (int i = 0; i < newGeneration.size(); i++) {
                if (BaseController.rndm.nextDouble() < this.percentageMutation)
                    newGeneration.set(i, problem.mutate(newGeneration.get(i)));
            }
            newGeneration = newGeneration.stream().sorted(Comparator.comparing(problem::fitness)).collect(Collectors.toList());
            if (problem.fitness(newGeneration.get(0)) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                bestIndividual = new ArrayList<>(newGeneration.get(0));
            this.generation = newGeneration;
            var avgFitness = generation.stream().mapToDouble(problem::fitness).average().getAsDouble();
            actualGeneration++;
            return new AlgorithmResults(problem, newGeneration.get(0), avgFitness, problem.fitness(newGeneration.get(newGeneration.size()-1)), bestIndividual, actualGeneration);
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
