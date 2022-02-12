package model.algorithms;

import controllers.base.BaseController;
import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class ArtificialBeeColonyAlgorithm implements Algorithm{
    private List<List<Integer>> generation = new ArrayList<>();
    private List<Integer> oldCount = new ArrayList<>();
    private List<Integer> bestIndividual = new ArrayList<>();
    private Problem problem;
    private Integer actualGeneration = 0;
    private Integer sizeBeeHive;
    private Integer numberOfIterations;
    private Integer forgetCount;
    private Double percentageEmployed;

    public void setAlgorithm(Integer sizeBeeHive, Integer numberOfIterations, Integer forgetCount, Double percentageEmployed) {
        this.sizeBeeHive = sizeBeeHive;
        this.numberOfIterations = numberOfIterations;
        this.forgetCount = forgetCount;
        this.percentageEmployed = percentageEmployed;
        resetAlgorithm();
    }

    @Override
    public void initFirstGeneration() {
        for (int i = 0; i < this.sizeBeeHive * percentageEmployed; i++) {
            generation.add(problem.makeOneIndividual());
        }
    }

    public Integer rouletteSelection() {
        double[] cumulativeFitnesses = new double[generation.size()];
        cumulativeFitnesses[0] = problem.fitness(generation.get(0));
        for (int i = 1; i < generation.size(); i++)
        {
            cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + problem.fitness(generation.get(i));
        }

        // this code was inspired by https://github.com/dwdyer/watchmaker/blob/master/framework/src/java/main/org/uncommons/watchmaker/framework/selection/RouletteWheelSelection.java
        double randomFitness = BaseController.rndm.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
        int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
        if (index < 0)
        {
            index = Math.abs(index + 1);
        }

        return index;
    }

    @Override
    public AlgorithmResults nextGeneration() {
        if (actualGeneration < numberOfIterations) {
            var generationBest = generation.stream().min(Comparator.comparing(problem::fitness)).get();
            if (problem.fitness(generationBest) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                bestIndividual = new ArrayList<>(generationBest);

            //EMPLOYED BEES PHASE
            for (int i = 0; i < generation.size(); i++) {
                var newPotentialIndividual = problem.mutate(generation.get(i));

                if (problem.fitness(newPotentialIndividual) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                    bestIndividual = new ArrayList<>(newPotentialIndividual);

                if (problem.fitness(generation.get(i)) > problem.fitness(newPotentialIndividual)) {
                    generation.set(i, newPotentialIndividual);
                    oldCount.set(i, 0);
                }
                else
                    oldCount.set(i, oldCount.get(i) + 1);
            }

            //ONLOOKER BEES PHASE
            for (int i = 0; i < generation.size()*(1-percentageEmployed); i++) {
                var index = rouletteSelection();
                var newPotentialIndividual = problem.mutate(generation.get(index));

                if (problem.fitness(newPotentialIndividual) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                    bestIndividual = new ArrayList<>(newPotentialIndividual);

                if (problem.fitness(generation.get(index)) > problem.fitness(newPotentialIndividual)) {
                    generation.set(index, newPotentialIndividual);
                    oldCount.set(i, 0);
                }
                else
                    oldCount.set(index, oldCount.get(index) + 1);
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
            return new AlgorithmResults(problem, generationBest, avgFitness, bestIndividual, actualGeneration);
        } else
            return null;
    }

    @Override
    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
        this.oldCount = new ArrayList<>(Collections.nCopies((int) Math.round(sizeBeeHive*percentageEmployed), 0));
        BaseController.rndm = new Random(1);
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
