package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import model.utils.AlgorithmResults;
import model.utils.DistinctColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TargetAssignmentProblem implements Problem{
    private List<List<Double>> matrixOfProbabilities;
    private List<Double> targetValues;
    private Integer maximumAssignedTargets;
    private Integer numOfTargets;
    private Integer numOfWeapons;
    private List<Color> colorsOfItems;

    public void populateProblem(Integer numOfTargets, Integer numOfWeapons, Integer maximumAssignedTargets) {
        matrixOfProbabilities = new ArrayList<>(numOfWeapons);
        targetValues = new ArrayList<>(numOfTargets);
        colorsOfItems = new ArrayList<>();
        for (int i = 0; i < numOfWeapons; i++) {
            matrixOfProbabilities.add(new ArrayList<>(numOfTargets));
            for (int j = 0; j < numOfTargets; j++) {
                matrixOfProbabilities.get(i).add(j, BaseController.rndm.nextInt(8)/10.0);
            }
        }
        for (int j = 0; j < numOfTargets; j++) {
            targetValues.add((double) (BaseController.rndm.nextInt(5) + 1));
        }
        this.maximumAssignedTargets = maximumAssignedTargets;
        this.numOfWeapons = numOfWeapons;
        this.numOfTargets = numOfTargets;

        for (int i = 0; i < numOfWeapons; i++) {
            var item = Color.web(DistinctColors.colors[BaseController.rndm.nextInt(DistinctColors.colors.length)]);
            this.colorsOfItems.add(item);
        }
    }
    
    public List<Integer> makeOneIndividual() {
        var individual = new ArrayList<Integer>();
        for (int i = 0; i < numOfWeapons; i++) {
            for (int j = 0; j < BaseController.rndm.nextInt(maximumAssignedTargets) + 1; j++) {
                individual.add(i);
                individual.add(BaseController.rndm.nextInt(numOfTargets));
            }
        }
        return individual;
    }

    @Override
    public Double fitness(List<Integer> individual) {
        var actualDes = new ArrayList<>(targetValues);
        for (int i = 0; i < individual.size() - 1; i+=2) {
            actualDes.set(individual.get(i+1), (actualDes.get(individual.get(i+1)) * matrixOfProbabilities.get(individual.get(i)).get(individual.get(i + 1))));
        }

        return (actualDes.stream().reduce(0.0, Double::sum));
    }

    @Override
    public List<Integer> mutate(List<Integer> individual) {
        var index = BaseController.rndm.nextInt((individual.size() - 1) /2) * 2 + 1;
        Collections.swap(individual, index, index + 2);

        return individual;
    }

    @Override
    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var child1 = new ArrayList<Integer>();
        var index = BaseController.rndm.nextInt(numOfWeapons - 2) + 1;
        for (int i = 0; i < parent1.size()-1; i+=2) {
            if (parent1.get(i) < index) {
                child1.add(parent1.get(i));
                child1.add(parent1.get(i+1));
            } else {
                for (int j = 0; j < parent2.size(); j+=2) {
                    if (parent2.get(j) >= index) {
                        child1.add(parent2.get(j));
                        child1.add(parent2.get(j+1));
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

            for (int i = 0; i < best.size(); i+=2) {
                gc.setStroke(colorsOfItems.get(best.get(i)));
                gc.setLineWidth(matrixOfProbabilities.get(best.get(i)).get(best.get(i + 1))*6 + 1);
                gc.strokeLine(offsetW*(best.get(i) + 1) + SIZE/2, 360, offsetT*(best.get(i+1)+1) + SIZE/2, 100 + SIZE/2);
            }

            gc.setLineWidth(2);
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);

            for (int i = 1; i < numOfTargets+1; i++) {
                gc.fillOval(offsetT*i, 100, SIZE, SIZE);
                gc.fillText(targetValues.get(i-1).intValue()+"", offsetT*i + SIZE/3, 100 - SIZE/2);
            }

            for (int i = 1; i < numOfWeapons+1; i++) {
                gc.fillOval(offsetW*i, 360, SIZE, SIZE);
                gc.fillText(i+"", offsetW*i + SIZE/3, 360 + 2 * SIZE);
            }

        }
    }
}
