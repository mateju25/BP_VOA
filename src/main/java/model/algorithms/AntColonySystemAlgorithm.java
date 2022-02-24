package model.algorithms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.base.BaseController;
import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class AntColonySystemAlgorithm implements Algorithm {
    @JsonIgnore
    private Problem problem;
    @JsonIgnore
    private List<List<Integer>> generation = new ArrayList<>();
    @JsonIgnore
    private List<Integer> bestIndividual = new ArrayList<>();
    @JsonIgnore
    private Integer actualGeneration = 0;
    @JsonIgnore
    private List<List<Double>> matrixOfPheromone;
    private Integer numberOfAnts;
    private Integer numberOfIterations;
    private Double pheromoneVapor;
    private Double parameterAlpha;
    private Double parameterBeta;
    private Double parameterQ;

    @Override
    public void init(Map<String, String> parameters) {
        this.numberOfAnts = Integer.parseInt(parameters.get("numberOfAnts"));
        this.numberOfIterations = Integer.parseInt(parameters.get("numberOfIterations"));
        this.pheromoneVapor = Double.parseDouble(parameters.get("pheromone"));
        this.parameterAlpha = Double.parseDouble(parameters.get("parameterA"));
        this.parameterBeta = Double.parseDouble(parameters.get("parameterB"));
        this.parameterQ = Double.parseDouble(parameters.get("parameterQ"));
        resetAlgorithm();
    }

    public Map<Integer, Double> getProbabilityOfEdges(Integer from, List<Integer> notVisited) {
        var map = new HashMap<Integer, Double>();
        var sum = 0.0;
        for (Integer number : notVisited) {
            sum += Math.pow(matrixOfPheromone.get(from).get(number), parameterAlpha) + Math.pow(problem.getHeuristicValue(from, number), parameterBeta);
        }
        for (Integer number : notVisited) {
            map.put(number, (Math.pow(matrixOfPheromone.get(from).get(number), parameterAlpha) + Math.pow(problem.getHeuristicValue(from, number), parameterBeta)) / sum);
        }
        if (BaseController.randomGenerator.nextDouble() < parameterQ) {
            var max = map.values().stream().mapToDouble(e -> e).max().getAsDouble();
            HashMap<Integer, Double> finalMap = map;
            var key = map.keySet().stream().filter(e -> finalMap.get(e).equals(max)).findFirst().get();
            map = new HashMap<Integer, Double>();
            map.put(key, max);
        }
        return map;
    }

    public void localUpdateEdge(Integer fromCity, Integer newIndex) {
        var newVal = matrixOfPheromone.get(fromCity).get(newIndex) * (1 - pheromoneVapor) + pheromoneVapor * 0;
        matrixOfPheromone.get(fromCity).set(newIndex, newVal);
    }

    @Override
    public void initFirstGeneration() {
        matrixOfPheromone = problem.initPheromoneMatrix();
        for (int i = 0; i < this.numberOfAnts; i++) {
            generation.add(problem.makeOneIndividual(this));
        }
    }

    @Override
    public AlgorithmResults nextGeneration() {
        if (actualGeneration < numberOfIterations) {
            var generationBest = generation.stream().min(Comparator.comparing(problem::fitness)).get();
            if (problem.fitness(generationBest) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                bestIndividual = new ArrayList<>(generationBest);


            //update best
            var edges = problem.generateEdges(generationBest);
            for (int i = 0; i < matrixOfPheromone.size(); i++) {
                for (int j = 0; j < matrixOfPheromone.get(i).size(); j++) {
                    matrixOfPheromone.get(i).set(j, matrixOfPheromone.get(i).get(j) * (1 - pheromoneVapor) + pheromoneVapor * edges.get(i).get(j));
                }
            }

            List<List<Integer>> newGeneration = new ArrayList<>();
            while (newGeneration.size() < generation.size()) {
                newGeneration.add(problem.makeOneIndividual(this));
            }

            newGeneration = newGeneration.stream().sorted(Comparator.comparing(problem::fitness)).collect(Collectors.toList());
            if (problem.fitness(newGeneration.get(0)) < problem.fitness(bestIndividual) || bestIndividual.size() == 0)
                bestIndividual = new ArrayList<>(newGeneration.get(0));
            this.generation = newGeneration;
            var avgFitness = generation.stream().mapToDouble(problem::fitness).average().getAsDouble();
            actualGeneration++;
            return new AlgorithmResults(problem, newGeneration.get(0), avgFitness, bestIndividual, actualGeneration, numberOfIterations);
        } else
            return null;
    }

    @Override
    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
    }

    @Override
    public String nameForFaces() {
        return "Ant Colony System Algorithm";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "ACSPage.fxml";
        return arr;
    }
}
