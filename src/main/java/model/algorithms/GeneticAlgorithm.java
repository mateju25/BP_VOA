package model.algorithms;

import controllers.base.BaseController;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
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
    private Integer typeOfCrossover;


    @Override
    public void init(Map<String, String> parameters) {
        this.numOfIndividuals = Integer.parseInt(parameters.get("numberIndividuals"));
        this.numOfGenerations = Integer.parseInt(parameters.get("numberGenerations"));
        this.percentageRoulette = Double.parseDouble(parameters.get("percentageRoulette"));
        this.percentageTournament = Double.parseDouble(parameters.get("percentageTournament"));
        this.sizeTournament = Integer.parseInt(parameters.get("sizeTournament"));
        this.percentageElitism = Double.parseDouble(parameters.get("percentageElitism"));
        this.percentageMutation = Double.parseDouble(parameters.get("percentageMutation"));
        this.typeOfCrossover = Integer.parseInt(parameters.get("typeCrossover"));
        resetAlgorithm();
    }

    @Override
    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
    }

    @Override
    public void initFirstGeneration() {
        for (int i = 0; i < this.numOfIndividuals; i++) {
            generation.add(problem.makeOneIndividual());
        }
    }

    private List<List<Integer>> makeChildrenWithCrossover(List<List<Integer>> parents) {
        var children = new ArrayList<List<Integer>>(2);
        Pair<List<Integer>, List<Integer>> resCrossover;
        if (typeOfCrossover == 0)
            resCrossover = problem.simpleCrossover(parents.get(0), parents.get(1));
        else
            resCrossover = problem.doubleCrossover(parents.get(0), parents.get(1));

        children.add(resCrossover.getKey());
        children.add(resCrossover.getValue());
        return children;
    }

    private List<List<Integer>> tournamentSelection() {
        var tournamentMembers = new ArrayList<List<Integer>>();
        BaseController.randomGenerator.ints(0, generation.size()).limit(sizeTournament).forEach(index -> tournamentMembers.add(generation.get(index)));

        var parents = tournamentMembers.stream().sorted(Comparator.comparing(problem::fitness)).limit(2).collect(Collectors.toList());

        return makeChildrenWithCrossover(parents);
    }

    private List<List<Integer>> rouletteSelection() {
        var parents = new ArrayList<List<Integer>>(2);
        for (int i = 0; i < 2; i++) {
            int index = Algorithm.getCumulativeFitnessesIndex(generation.stream().mapToDouble(e -> problem.fitness(e)).boxed().collect(Collectors.toList()));
            parents.add(generation.get(index));
        }
        return makeChildrenWithCrossover(parents);
    }

    @Override
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
                if (BaseController.randomGenerator.nextDouble() < this.percentageMutation)
                    newGeneration.set(i, problem.mutate(newGeneration.get(i)));
            }
            newGeneration = newGeneration.stream().sorted(Comparator.comparing(problem::fitness)).collect(Collectors.toList());
            if (problem.fitness(newGeneration.get(0)) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                bestIndividual = new ArrayList<>(newGeneration.get(0));
            this.generation = newGeneration;
            var avgFitness = generation.stream().mapToDouble(problem::fitness).average().getAsDouble();
            actualGeneration++;
            return new AlgorithmResults(problem, newGeneration.get(0), avgFitness, bestIndividual, actualGeneration, numOfGenerations);
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
