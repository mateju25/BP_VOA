package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import model.utils.AlgorithmResults;
import model.utils.DistinctColors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TargetAssignmentProblem implements Problem {
    private List<List<Double>> matrixOfProbabilities;
    private List<Double> targetValues;
    private Integer maximumAssignedTargets;
    private Integer numOfTargets;
    private Integer numOfWeapons;
    private List<Color> colorsOfItems;

    @Override
    public void init(Map<String, String> parameters) {
        this.numOfTargets = Integer.parseInt(parameters.get("numberOfTargets"));
        this.numOfWeapons = Integer.parseInt(parameters.get("numberOfWeapons"));
        this.maximumAssignedTargets = Integer.parseInt(parameters.get("maxAssignedTargets"));

        matrixOfProbabilities = new ArrayList<>(numOfWeapons);
        targetValues = new ArrayList<>(numOfTargets);
        colorsOfItems = new ArrayList<>();
        for (int i = 0; i < numOfWeapons; i++) {
            matrixOfProbabilities.add(new ArrayList<>(numOfTargets));
            for (int j = 0; j < numOfTargets; j++) {
                matrixOfProbabilities.get(i).add(j, BaseController.randomGenerator.nextInt(8) / 10.0);
            }
        }
        for (int j = 0; j < numOfTargets; j++) {
            targetValues.add((double) (BaseController.randomGenerator.nextInt(5) + 1));
        }

        for (int i = 0; i < numOfWeapons; i++) {
            var item = Color.web(DistinctColors.colors[BaseController.randomGenerator.nextInt(DistinctColors.colors.length)]);
            this.colorsOfItems.add(item);
        }
    }

    public List<Integer> makeOneIndividual() {
        var individual = new ArrayList<Integer>();
        for (int i = 0; i < numOfWeapons; i++) {
            var targetIndexes = Arrays.stream(IntStream.range(0, numOfTargets).toArray()).boxed().collect(Collectors.toList());
            for (int j = 0; j < BaseController.randomGenerator.nextInt(maximumAssignedTargets) + 1; j++) {
                if (targetIndexes.size() == 0)
                    break;
                individual.add(i);
                var index = BaseController.randomGenerator.nextInt(targetIndexes.size());
                individual.add(targetIndexes.get(index));
                targetIndexes.remove(index);
            }
        }
        return individual;
    }

    @Override
    public Double fitness(List<Integer> individual) {
        var actualDes = new ArrayList<>(targetValues);
        for (int i = 0; i < individual.size() - 1; i += 2) {
            actualDes.set(individual.get(i + 1), (actualDes.get(individual.get(i + 1)) * matrixOfProbabilities.get(individual.get(i)).get(individual.get(i + 1))));
        }

        return (actualDes.stream().reduce(0.0, Double::sum));
    }

    @Override
    public List<Integer> mutate(List<Integer> individual) {
        var index = BaseController.randomGenerator.nextInt((individual.size() - 1) / 2) * 2 + 1;
        Collections.swap(individual, index, index + 2);

        return individual;
    }

    @Override
    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var child1 = new ArrayList<Integer>();
        var index = BaseController.randomGenerator.nextInt(numOfWeapons - 2) + 1;
        for (int i = 0; i < parent1.size() - 1; i += 2) {
            if (parent1.get(i) < index) {
                child1.add(parent1.get(i));
                child1.add(parent1.get(i + 1));
            } else {
                for (int j = 0; j < parent2.size(); j += 2) {
                    if (parent2.get(j) >= index) {
                        child1.add(parent2.get(j));
                        child1.add(parent2.get(j + 1));
                    }
                }
                break;
            }
        }
        return new Pair<>(child1, null);
    }

    @Override
    public String nameForFaces() {
        return "Target Assignment Problem";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "TAPPage.fxml";
        return arr;
    }

    @Override
    public void visualize(Canvas canvas, AlgorithmResults data) {
        if (data != null) {
            var gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.web("#F7EDE2"));
            gc.fillRect(-5, -5, canvas.getWidth() + 5, canvas.getHeight() + 5);


            var SIZE = 20;
            var offsetW = canvas.getWidth() / (numOfWeapons + 2);
            var offsetT = canvas.getWidth() / (numOfTargets + 2);

            var best = data.getBestIndividual();

            for (int i = 0; i < best.size(); i += 2) {
                gc.setStroke(colorsOfItems.get(best.get(i)));
                gc.setLineWidth(matrixOfProbabilities.get(best.get(i)).get(best.get(i + 1)) * 6 + 1);
                gc.strokeLine(offsetW * (best.get(i) + 1) + (int) (SIZE / 2), 360, offsetT * (best.get(i + 1) + 1) + (int) (SIZE / 2), 100 + (int) (SIZE / 2));
            }

            gc.setLineWidth(2);
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);

            gc.setLineWidth(1);
            gc.strokeText("Targets ", 20, 70);

            for (int i = 1; i < numOfTargets + 1; i++) {
                var scale = 5 - targetValues.get(i - 1).intValue();
                gc.setFill(Color.rgb((50 * scale), (50 * scale), (50 * scale)));
                gc.fillOval(offsetT * i, 100, SIZE, SIZE);
                gc.fillText(targetValues.get(i - 1).intValue() + "", offsetT * i + (int) (SIZE / 3), 100 - (int) (SIZE / 3));
            }

            gc.setLineWidth(1);
            gc.strokeText("Weapons ", 20, 400);

            gc.setFill(Color.BLACK);

            for (int i = 1; i < numOfWeapons + 1; i++) {
                gc.fillOval(offsetW * i, 360, SIZE, SIZE);
            }

        }
    }
}
