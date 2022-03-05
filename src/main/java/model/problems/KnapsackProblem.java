package model.problems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.utils.AlgorithmResults;
import model.utils.DistinctColors;

import java.beans.Transient;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of 0-1 knapsack problem
 */
@Getter
@Setter
public class KnapsackProblem implements Problem {
    @JsonIgnore
    private List<Integer> itemWeight;
    @JsonIgnore
    private List<Integer> itemValue;
    private Integer weightOfBackpack;
    @JsonIgnore
    private List<Color> colorsOfItems;
    private Integer averageWeightOfItem;
    private Integer numberOfItems;

    /**
     * Initializes problem with parameters.
     * @param parameters parameters from input fields.
     */
    @Override
    public void init(Map<String, String> parameters) {
        this.numberOfItems = Integer.parseInt(parameters.get("numberOfItems"));
        this.averageWeightOfItem = Integer.parseInt(parameters.get("averageWeight"));
        this.weightOfBackpack = Integer.parseInt(parameters.get("backpackCapacity"));
    }

    /**
     * Regenerates problem. Creates weight and values of all items.
     */
    @Override
    public void regenerate() {
        this.itemWeight = new ArrayList<>();
        this.itemValue = new ArrayList<>();
        this.colorsOfItems = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            var item = BaseController.randomGenerator.nextInt(2 * averageWeightOfItem) + 1;
            this.itemWeight.add(item);
            item = BaseController.randomGenerator.nextInt(4 * averageWeightOfItem) + 1;
            this.itemValue.add(item);
        }
        for (int i = 0; i < numberOfItems; i++) {
            var item = Color.web(DistinctColors.colors[BaseController.randomGenerator.nextInt(DistinctColors.colors.length)]);
            this.colorsOfItems.add(item);
        }
    }

    /**
     * Initializes matrix of pheromones with dimension of the problem, used in ACS
     * @return matrix of pheromones
     */
    @Override
    public List<List<Double>> initPheromoneMatrix() {
        ArrayList<List<Double>> matrix = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            ArrayList<Double> lst = new ArrayList<>(Collections.nCopies(numberOfItems, 0.0));
            matrix.add(lst);
        }
        return matrix;
    }

    /**
     * @param items solution of the problem
     * @return return sum of item weights
     */
    private Integer sumOfItems(List<Integer> items) {
        var sum = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == 1)
                sum += itemWeight.get(i);
        }
        return sum;
    }

    /**
     * @param items solution of the problem
     * @return return sum of item values
     */
    private Integer sumOfValues(List<Integer> items) {
        var sum = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == 1)
                sum += itemValue.get(i);
        }
        return sum;
    }

    /**
     * Make one solution of the problem, used in GA, ABC
     * @return individual
     */
    @Override
    public List<Integer> makeOneIndividual() {
        var individual = new ArrayList<Integer>();
        var threshold = 0.5;
        if (this.itemWeight.size() * averageWeightOfItem > weightOfBackpack)
            threshold = weightOfBackpack / (this.itemWeight.size() * 1.0 * averageWeightOfItem);
        do {
            individual = new ArrayList<>();
            for (int i = 0; i < itemWeight.size(); i++) {
                if (BaseController.randomGenerator.nextDouble() < threshold * 0.5)
                    individual.add(1);
                else
                    individual.add(0);
            }
            threshold /= 2;
        } while (sumOfItems(individual) > weightOfBackpack);
        return individual;
    }

    /**
     * Make one solution of the problem, used in ACS
     * @param acs ACS algorithm instance
     * @return individual
     */
    @Override
    public List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs) {
        var freeItems = IntStream.range(0, numberOfItems).boxed().collect(Collectors.toList());
        var fromNode = freeItems.get(BaseController.randomGenerator.nextInt(freeItems.size()));
        freeItems.remove(fromNode);

        var newIndividual = new ArrayList<Integer>();
        newIndividual.add(fromNode);
        var currentWeight = itemWeight.get(fromNode);
        while (freeItems.size() > 0) {
            var probabilities = acs.getProbabilityOfEdges(fromNode, freeItems);
            var probList = probabilities.values().stream().mapToDouble(e -> e).boxed().collect(Collectors.toList());

            int index = Algorithm.getCumulativeFitnessesIndex(probList);

            Integer newIndex = 0;
            for (Integer key : probabilities.keySet()) {
                if (probabilities.get(key).equals(probList.get(index)))
                    newIndex = key;
            }

            if (currentWeight + itemWeight.get(newIndex) > weightOfBackpack) {
                break;
            }
            currentWeight += itemWeight.get(newIndex);

            freeItems.remove(newIndex);
            newIndividual.add(newIndex);

            acs.localUpdateEdge(fromNode, newIndex);
            acs.localUpdateEdge(newIndex, fromNode);

            fromNode = newIndex;
        }
        var individual = new ArrayList<>(Collections.nCopies(numberOfItems, 0));
        for (Integer number : newIndividual) {
            individual.set(number, 1);
        }
        return individual;
    }

    /**
     * Generates edges that will have increased pheromone level
     * @param individual best individual in iteration
     * @return matrix of pheromones
     */
    @Override
    public List<List<Double>> generateEdges(List<Integer> individual) {
        var edges = initPheromoneMatrix();
        var fitness = fitness(individual);
        var newIndividual = new ArrayList<Integer>();
        for (int i = 0; i < individual.size(); i++) {
            if (individual.get(i) == 1)
                newIndividual.add(i);
        }
        for (int i = 0; i < newIndividual.size() - 1; i++) {
            edges.get(newIndividual.get(i)).set(newIndividual.get(i + 1), fitness);
        }
        return edges;
    }

    /**
     * @param from start node
     * @param to to node
     * @return heuristic value of one line in graph
     */
    @Override
    public Double getHeuristicValue(Integer from, Integer to) {
        return itemValue.get(to) * 0.01;
    }

    /**
     * @param individual solution of the problem
     * @return fitness value of solution
     */
    @Override
    public Double fitness(List<Integer> individual) {
        return 1.0 / (sumOfValues(individual) + 1.0) * numberOfItems;
    }

    /**
     * Changes individual, used in GA
     * @param individual solution of the problem
     * @return mutated individual
     */
    @Override
    public List<Integer> mutate(List<Integer> individual) {
        var tmpIndividual = new ArrayList<>(individual);
        var threshold = 0.5;
        do {
            individual = new ArrayList<>(tmpIndividual);
            var index = BaseController.randomGenerator.nextInt(this.itemWeight.size());
            if (BaseController.randomGenerator.nextDouble() < threshold)
                individual.set(index, ((individual.get(index)) + 1) % 2);
            else
                individual.set(index, 0);
            threshold /= 2;
        } while (sumOfItems(individual) > weightOfBackpack);
        return individual;
    }

    /**
     * Changes individual, used in ABC
     * @param individual one solution of the problem
     * @param probChange strength of the mutation
     * @return changed individual
     */
    @Override
    public List<Integer> localSearch(List<Integer> individual, Double probChange) {
        var tmpIndividual = new ArrayList<>(individual);
        var threshold = 0.5;
        do {
            individual = new ArrayList<>(tmpIndividual);
            for (int i = 0; i < numberOfItems * probChange; i++) {
                var index = BaseController.randomGenerator.nextInt(this.itemWeight.size());
                if (individual.get(index).equals(0)) {
                    if (sumOfItems(individual) + itemWeight.get(index) > weightOfBackpack)
                        continue;
                }
                if (BaseController.randomGenerator.nextDouble() < threshold)
                    individual.set(index, ((individual.get(index)) + 1) % 2);
                else
                    individual.set(index, 0);
            }
            threshold /= 2;
        } while (sumOfItems(individual) > weightOfBackpack);
        return individual;
    }

    /**
     * Creates two children by single point cross-overing parents, used in GA
     * @param parent1 one solution of the problem
     * @param parent2 second solution of the problem
     * @return pair of children
     */
    @Override
    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var child1 = new ArrayList<Integer>();
        var child2 = new ArrayList<Integer>();
        do {
            var index = BaseController.randomGenerator.nextInt(itemWeight.size());
            child1 = new ArrayList<>();
            child2 = new ArrayList<>();
            for (int i = 0; i < itemWeight.size(); i++) {
                if (i < index) {
                    child1.add(parent1.get(i));
                    child2.add(parent2.get(i));
                } else {
                    child2.add(parent1.get(i));
                    child1.add(parent2.get(i));
                }
            }
        } while ((sumOfItems(child1) > weightOfBackpack) || (sumOfItems(child2) > weightOfBackpack));
        return new Pair<>(child1, child2);
    }

    /**
     * Creates two children by double point cross-overing parents, used in GA
     * @param parent1 one solution of the problem
     * @param parent2 second solution of the problem
     * @return pair of children
     */
    @Override
    public Pair<List<Integer>, List<Integer>> doubleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var child1 = new ArrayList<Integer>();
        var child2 = new ArrayList<Integer>();
        do {
            var index = BaseController.randomGenerator.nextInt(itemWeight.size());
            var indexSecond = BaseController.randomGenerator.nextInt(itemWeight.size() - index) + index;
            child1 = new ArrayList<>();
            child2 = new ArrayList<>();
            for (int i = 0; i < itemWeight.size(); i++) {
                if (i < index) {
                    child1.add(parent1.get(i));
                    child2.add(parent2.get(i));
                } else {
                    if (i < indexSecond) {
                        child2.add(parent1.get(i));
                        child1.add(parent2.get(i));
                    } else {
                        child1.add(parent1.get(i));
                        child2.add(parent2.get(i));
                    }
                }
            }
        } while ((sumOfItems(child1) > weightOfBackpack) || (sumOfItems(child2) > weightOfBackpack));
        return new Pair<>(child1, child2);
    }

    /**
     * @return message that will be displayed in simulation.
     */
    @Override
    public String nameForFaces() {
        return "Knapsack Problem";
    }

    /**
     * @return component that will controller need.
     */
    @Override
    public String nameOfFxmlFiles() {
        return "KPPage.fxml";
    }

    /**
     * Visualize best solution to the provided canvas.
     * @param canvas visualization place
     * @param data algorithm results
     */
    @Override
    public void visualize(Canvas canvas, AlgorithmResults data) {
        if (data != null) {
            var gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.web("#faf4ee"));
            gc.fillRect(-5, -5, canvas.getWidth() + 5, canvas.getHeight() + 5);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(5);
            var HEIGHT_OF_CONTAINER = 450;
            var WIDTH_OF_CONTAINER = 800;
            gc.strokeLine(100, 20, 100 + WIDTH_OF_CONTAINER, 20);
            gc.strokeLine(100, HEIGHT_OF_CONTAINER + 20, 100 + WIDTH_OF_CONTAINER, HEIGHT_OF_CONTAINER + 20);
            gc.strokeLine(100, 20, 100, HEIGHT_OF_CONTAINER + 20);
            gc.strokeLine(100 + WIDTH_OF_CONTAINER, 20, 100 + WIDTH_OF_CONTAINER, HEIGHT_OF_CONTAINER + 20);

            var best = data.getBestIndividual();

            gc.setLineWidth(1);
            gc.strokeText("Capacity: " + (int) (sumOfItems(best) / (weightOfBackpack * 1.0) * 100) + "%" + " Value: " + (int) ((sumOfValues(best) * 1.0) / itemValue.stream().mapToInt(e -> e).sum() * 100) + "%", 80 + WIDTH_OF_CONTAINER / 2, HEIGHT_OF_CONTAINER + 40);

            var level = 0;
            for (int i = 0; i < best.size(); i++) {
                if (best.get(i) == 0)
                    continue;
                var weight = itemWeight.get(i);
                gc.setFill(colorsOfItems.get(i));
                gc.fillRect(100 + level, 20, Math.round(((weight * 1.0) / weightOfBackpack) * WIDTH_OF_CONTAINER), HEIGHT_OF_CONTAINER);
                level += Math.round(((weight * 1.0) / weightOfBackpack) * WIDTH_OF_CONTAINER);
            }
        }
    }

    /**
     * Sets parameters.
     * @param number index of preset problem
     */
    @Override
    public void setPreset(Integer number) {
        var params = new HashMap<String, String>();
        switch (number) {
            case 0: {
                params.put("numberOfItems", "10");
                params.put("averageWeight", "2");
                params.put("backpackCapacity", "10");
                break;
            }
            case 1: {
                params.put("numberOfItems", "100");
                params.put("averageWeight", "3");
                params.put("backpackCapacity", "200");
                break;
            }
            case 2: {
                params.put("numberOfItems", "1000");
                params.put("averageWeight", "4");
                params.put("backpackCapacity", "800");
                break;
            }
        }
        init(params);
    }
}
