package model.algorithms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class ArtificialBeeColonyAlgorithm implements Algorithm {
    @JsonIgnore
    private List<List<Integer>> generation = new ArrayList<>();
    @JsonIgnore
    private List<Integer> oldCount = new ArrayList<>();
    @JsonIgnore
    private List<Integer> bestIndividual = new ArrayList<>();
    @JsonIgnore
    private Problem problem;
    @JsonIgnore
    private Integer actualGeneration = 0;
    private Integer sizeBeeHive;
    private Integer numberOfIterations;
    private Integer forgetCount;
    private Double percentageEmployed;
    private Double mutationUpper;
    private Double mutationLower;


    @Override
    public void init(Map<String, String> parameters) {
        this.sizeBeeHive = Integer.parseInt(parameters.get("sizeBeeHive"));
        this.numberOfIterations = Integer.parseInt(parameters.get("numberOfIterations"));
        this.forgetCount = Integer.parseInt(parameters.get("forgetCount"));
        this.percentageEmployed = Double.parseDouble(parameters.get("employedBees"));
        this.mutationUpper = Double.parseDouble(parameters.get("mutationUpper"));
        this.mutationLower = Double.parseDouble(parameters.get("mutationLower"));
        resetAlgorithm();
    }

    @Override
    public void initFirstGeneration() {
        for (int i = 0; i < this.sizeBeeHive * percentageEmployed; i++) {
            generation.add(problem.makeOneIndividual());
        }
    }

    public Integer rouletteSelection() {
        return Algorithm.getCumulativeFitnessesIndex(generation.stream().mapToDouble(e -> problem.fitness(e)).boxed().collect(Collectors.toList()));
    }

    private void takeBetterIndividual(Integer index, Integer oldCountIndex) {
        var individual = problem.localSearch(generation.get(index), mutationLower + (actualGeneration / numberOfIterations) * (mutationUpper - mutationLower));
        if (problem.fitness(individual) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
            bestIndividual = new ArrayList<>(individual);

        if (problem.fitness(generation.get(index)) > problem.fitness(individual)) {
            generation.set(index, individual);
            oldCount.set(oldCountIndex, 0);
        } else
            oldCount.set(index, oldCount.get(index) + 1);
    }

    @Override
    public AlgorithmResults nextGeneration() {
        if (actualGeneration < numberOfIterations) {
            var generationBest = generation.stream().min(Comparator.comparing(problem::fitness)).get();
            if (problem.fitness(generationBest) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                bestIndividual = new ArrayList<>(generationBest);

            //EMPLOYED BEES PHASE
            for (int i = 0; i < generation.size(); i++) {
                takeBetterIndividual(i, i);
            }

            //ONLOOKER BEES PHASE
            for (int i = 0; i < generation.size() * (1 - percentageEmployed); i++) {
                var index = rouletteSelection();
                takeBetterIndividual(index, i);
            }

            //SCOUT BEES PHASE
            for (int i = 0; i < generation.size(); i++) {
                if (oldCount.get(i) > forgetCount) {
                    var newIndividual = problem.makeOneIndividual();
                    generation.set(i, newIndividual);

                    if (problem.fitness(newIndividual) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                        bestIndividual = new ArrayList<>(newIndividual);
                }
            }

            var avgFitness = generation.stream().mapToDouble(problem::fitness).average().getAsDouble();
            actualGeneration++;
            return new AlgorithmResults(problem, generationBest, avgFitness, bestIndividual, actualGeneration, numberOfIterations);
        } else
            return null;
    }

    @Override
    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
        this.oldCount = new ArrayList<>(Collections.nCopies((int) Math.round(sizeBeeHive * percentageEmployed), 0));
    }

    @Override
    public String nameForFaces() {
        return "Artificial Bee Colony Algorithm";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "ABCPage.fxml";
        return arr;
    }
}
