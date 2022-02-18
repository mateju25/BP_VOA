package model.algorithms;

import controllers.base.BaseController;
import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class AntColonySystemAlgorithm implements Algorithm {
    private Problem problem;
    private List<List<Integer>> generation = new ArrayList<>();
    private List<Integer> bestIndividual = new ArrayList<>();
    private Integer actualGeneration = 0;
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

    public Double getProbabilityOfEdgeToSelect(Integer from, Integer to, List<Integer> notVisited) {

        if (BaseController.randomGenerator.nextDouble() < parameterQ) {
            for (int i = 0; i < bestIndividual.size() - 1; i++) {
                if (bestIndividual.get(i).equals(from) && bestIndividual.get(i + 1).equals(to))
                    notVisited.remove(to);
            }
        }
        var sum = 0.0;
        notVisited = notVisited.stream().filter(e -> !e.equals(to)).collect(Collectors.toList());
        for (Integer number : notVisited) {
            sum += Math.pow(matrixOfPheromone.get(from).get(number), parameterAlpha) + Math.pow(problem.getHeuristicValue(from, number), parameterBeta);
        }
        return (Math.pow(matrixOfPheromone.get(from).get(to), parameterAlpha) + Math.pow(problem.getHeuristicValue(from, to), parameterBeta)) / sum;
    }

    public Double newProbValueDueToValue(Integer from, Integer to, List<Double> heuristic) {
        return pheromoneVapor * matrixOfPheromone.get(from).get(to) +
                (1-pheromoneVapor)*heuristic.stream().min(Double::compareTo).get();
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


            //update all
//            for (List<Integer> individual: generation ) {
//                var fitness = problem.fitness(individual);
//                for (int i = 0; i < individual.size()-1; i++) {
//                    var newVal = matrixOfPheromone.get(individual.get(i)).get(individual.get(i+1)) * pheromoneVapor;
////                            + (1-pheromoneVapor)*problem.getHeuristicValue(individual.get(i), individual.get(i+1));
//                    matrixOfPheromone.get(individual.get(i)).set(individual.get(i+1), newVal);
//                }
//            }

            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
            decimalFormat.setMinimumFractionDigits(2);
            for (List<Double> lst : matrixOfPheromone) {
                for (Double doub: lst){
                    System.out.print(decimalFormat.format(doub) + " ");
                }
                System.out.println();
            }
            generation.forEach(System.out::println);
            System.out.println("-------------------------------------------------------------------------------");


            //update best in generation
            var fitness = problem.fitness(generationBest);
            for (int i = 0; i < generationBest.size()-1; i++) {
                matrixOfPheromone.get(generationBest.get(i)).set(generationBest.get(i+1), problem.getHeuristicValue(generationBest.get(i), generationBest.get(i+1)));
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
            return new AlgorithmResults(problem, newGeneration.get(0), avgFitness, bestIndividual, actualGeneration);
        } else
            return null;
    }

    @Override
    public void resetAlgorithm() {
        this.actualGeneration = 0;
        this.generation = new ArrayList<>();
        this.bestIndividual = new ArrayList<>();
        BaseController.randomGenerator = new Random(1);
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
