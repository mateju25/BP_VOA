package model.problems;

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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
public class KnapsackProblem implements Problem {
    private List<Integer> itemWeight;
    private List<Integer> itemValue;
    private Integer weightOfBackpack;
    private List<Color> colorsOfItems;
    private Integer averageWeightOfItem;
    private Integer numberOfItems;

    @Override
    public void init(Map<String, String> parameters) {
        this.numberOfItems = Integer.parseInt(parameters.get("numberOfItems"));
        this.averageWeightOfItem = Integer.parseInt(parameters.get("averageWeight"));
        this.weightOfBackpack = Integer.parseInt(parameters.get("backpackCapacity"));
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

    @Override
    public List<List<Double>> initPheromoneMatrix() {
        ArrayList<List<Double>> matrix = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            ArrayList<Double> lst = new ArrayList<>(Collections.nCopies(numberOfItems, 0.0));
            matrix.add(lst);
        }
        return matrix;
    }

    public Integer sumOfItems(List<Integer> items) {
        var sum = 0;
        for (Integer item : items) {
            sum += itemWeight.get(item);
        }
        return sum;
    }

    public Integer sumOfValues(List<Integer> items) {
        var sum = 0;
        for (Integer item : items) {
            sum += itemValue.get(item);
        }
        return sum;
    }

    public List<Integer> makeOneIndividual() {
        var individual = new ArrayList<Integer>();
        var newIndividual = new ArrayList<Integer>();
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
            newIndividual = new ArrayList<Integer>();
            for (int i = 0; i < individual.size(); i++) {
                if (individual.get(i) == 1)
                    newIndividual.add(i);
            }
        } while (sumOfItems(newIndividual) > weightOfBackpack || sumOfItems(newIndividual) == 0);

        return newIndividual;
    }

    public List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs) {
        var individual = new ArrayList<Integer>();
        individual.add(0);
        var needToVisitNodes = IntStream.range(1, numberOfItems).boxed().collect(Collectors.toList());
        var currentWeight = itemWeight.get(0);
        var fromNode = individual.get(individual.size() - 1);
        while (true) {
            var probabilities = acs.getProbabilityOfEdges(fromNode, needToVisitNodes);
            var probList = probabilities.values().stream().mapToDouble(e -> e).boxed().collect(Collectors.toList());

            int index = Algorithm.getCumulativeFitnessesIndex(probList);

            Integer newIndex = 0;
            for (Integer key : probabilities.keySet()) {
                if (probabilities.get(key).equals(probList.get(index)))
                    newIndex = key;
            }

            if (currentWeight + itemWeight.get(newIndex) > weightOfBackpack)
                break;

            currentWeight += itemWeight.get(newIndex);

            needToVisitNodes.remove(newIndex);
            individual.add(newIndex);

            acs.localUpdateEdge(fromNode, newIndex);

            fromNode = newIndex;
        }
        return individual;
    }

    @Override
    public Double getHeuristicValue(Integer from, Integer to) {
        return itemValue.get(to)+0.0;
    }

    public Double fitness(List<Integer> individual) {
        return 1.0 / (sumOfValues(individual) * 1.0) * numberOfItems;
    }

    public List<Integer> mutate(List<Integer> individual) {
        var tmpIndividual = new ArrayList<>(individual);
        var notUsedPlaces = IntStream.range(0, numberOfItems).boxed().collect(Collectors.toList());
        for (Integer number : tmpIndividual) {
            notUsedPlaces.remove(number);
        }
        do {
            for (int i = 0; i < 5; i++) {
                var value = BaseController.randomGenerator.nextInt(notUsedPlaces.size());
                if (sumOfItems(individual) + itemWeight.get(notUsedPlaces.get(value)) < weightOfBackpack) {
                    individual.add(notUsedPlaces.get(value));
                    notUsedPlaces.remove(notUsedPlaces.get(value));
                }
                individual = new ArrayList<>(tmpIndividual);
                var index = BaseController.randomGenerator.nextInt(individual.size());
                value = BaseController.randomGenerator.nextInt(notUsedPlaces.size());
                individual.set(index, notUsedPlaces.get(value));
            }
        } while (sumOfItems(individual) > weightOfBackpack || sumOfItems(individual) == 0);
        return individual;
    }

    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var limit = Math.min(parent1.size(), parent2.size());

        var child1 = new ArrayList<Integer>();
        do {
            var newParent1 = new ArrayList<>(parent1);
            var newParent2 = new ArrayList<>(parent2);
            child1 = new ArrayList<Integer>();
            var index = BaseController.randomGenerator.nextInt(limit);
            for (int i = 0; i < parent1.size(); i++) {
                if (i < index) {
                    child1.add(newParent1.get(i));
                    newParent2.remove(newParent1.get(i));
                } else {
                    child1.addAll(newParent2);
                    break;
                }
            }
        } while ((sumOfItems(child1) > weightOfBackpack) || sumOfItems(child1) == 0);
        return new Pair<>(child1, null);
    }

    @Override
    public String nameForFaces() {
        return "Knapsack Problem";
    }

    @Override
    public String nameOfFxmlFiles() {
        return "KPPage.fxml";
    }

    @Override
    public void visualize(Canvas canvas, AlgorithmResults data) {
        if (data != null) {
            var gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.web("#F7EDE2"));
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
            gc.strokeText("Capacity: " + (int) (sumOfItems(best) / (weightOfBackpack * 1.0) * 100) + "%" + " Value: " + (int) ((sumOfValues(best) * 1.0) / itemValue.stream().mapToInt(e -> e).sum() * 100) + "%", (int) (80 + WIDTH_OF_CONTAINER / 2), HEIGHT_OF_CONTAINER + 40);

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
