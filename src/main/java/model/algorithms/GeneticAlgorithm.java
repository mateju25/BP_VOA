package model.algorithms;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

/**
 * Implementation of genetic algorithm
 */
@Setter
@Getter
public class GeneticAlgorithm implements Algorithm {
    @JsonIgnore
    private List<List<Integer>> generation = new ArrayList<>();
    @JsonIgnore
    private List<Integer> bestIndividual = new ArrayList<>();
    @JsonIgnore
    private Problem problem;
    @JsonIgnore
    private Integer actualGeneration = 0;
    private Integer numOfIndividuals;
    private Integer numOfGenerations;
    private Double percentageRoulette;
    private Double percentageTournament;
    private Integer sizeTournament;
    private Double percentageElitism;
    private Double percentageMutation;
    private Integer typeOfCrossover;

    /**
     * Initiliazes algorithm with parameters.
     * @param parameters parameters from input fields.
     */
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

    /**
     * Resets algorithm to the first generation.
     */
    @Override
    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
    }

    /**
     * Creates first generation.
     */
    @Override
    public void initFirstGeneration() {
        for (int i = 0; i < this.numOfIndividuals; i++) {
            generation.add(problem.makeOneIndividual());
        }
    }

    /**
     * @param parents parents that will be used in crossover
     * @return list of children after crossover
     */
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

    /**
     * @return result of tournament selection.
     */
    private List<List<Integer>> tournamentSelection() {
        var tournamentMembers = new ArrayList<List<Integer>>();
        BaseController.randomGenerator.ints(0, generation.size()).limit(sizeTournament).forEach(index -> tournamentMembers.add(generation.get(index)));

        var parents = tournamentMembers.stream().sorted(Comparator.comparing(problem::fitness)).limit(2).collect(Collectors.toList());

        return makeChildrenWithCrossover(parents);
    }

    /**
     *  @return result of roulette selection.
     */
    private List<List<Integer>> rouletteSelection() {
        var parents = new ArrayList<List<Integer>>(2);
        for (int i = 0; i < 2; i++) {
            int index = Algorithm.getCumulativeFitnessesIndex(generation.stream().mapToDouble(e -> problem.fitness(e)).boxed().collect(Collectors.toList()));
            parents.add(generation.get(index));
        }
        return makeChildrenWithCrossover(parents);
    }

    /**
     * Runs one iteration of GA algorithm.
     * @return results after one run.
     */
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

    /**
     * @return message that will be displayed in simulation.
     */
    @Override
    public String nameForFaces() {
        return "Genetic Algorithm";
    }

    /**
     * @return all components that will controller need.
     */
    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "GAPage.fxml";
        return arr;
    }
}
