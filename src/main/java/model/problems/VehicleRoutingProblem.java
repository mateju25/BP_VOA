package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;
import model.utils.AlgorithmResults;
import model.utils.DistinctColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleRoutingProblem implements Problem {
    @Getter
    private static class Point {
        private final Integer xCor;
        private final Integer yCor;
        private final Integer demand;

        public Point(Integer xCor, Integer yCor, Integer demand) {
            this.xCor = xCor;
            this.yCor = yCor;
            this.demand = demand;
        }
    }

    private List<Point> points;
    private List<List<Integer>> matrixOfDistances;
    private Integer vehicleCapacity;
    private Double fitnessCoef;
    private List<Color> colorsOfItems;


    public void populateProblem(Integer sizeOfTheProblem, Integer vehicleCapacity, Integer averageDemand) {
        points = new ArrayList<>();
        colorsOfItems = new ArrayList<>();
        points.add(new Point(250, 250, 0));
        for (int i = 0; i < sizeOfTheProblem - 1; i++) {
            var demand = BaseController.rndm.nextInt(2*averageDemand - 2) + 1;
            while (demand > vehicleCapacity)
                demand = BaseController.rndm.nextInt(2*averageDemand - 2) + 1;
            var tmp = new Point(BaseController.rndm.nextInt(500), BaseController.rndm.nextInt(500), demand);
            points.add(tmp);
        }
        for (int i = 0; i < sizeOfTheProblem; i++) {
            var item = Color.web(DistinctColors.colors[BaseController.rndm.nextInt(DistinctColors.colors.length)]);
            this.colorsOfItems.add(item);
        }
        matrixOfDistances = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            matrixOfDistances.add(new ArrayList<>(points.size()));
            for (int j = 0; j < points.size(); j++) {
                matrixOfDistances.get(i).add(j, 0);
            }
        }
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                var distance = Math.sqrt(Math.pow(Math.abs(points.get(i).xCor - points.get(j).xCor), 2) + Math.pow(Math.abs(points.get(i).yCor - points.get(j).yCor), 2));
                matrixOfDistances.get(i).set(j, (int) Math.round(distance));
                matrixOfDistances.get(j).set(i, (int) Math.round(distance));
            }
        }
        this.vehicleCapacity = vehicleCapacity;

        var tmpIndividual = makeOneIndividual();
        fitnessCoef = 1.0;
        var fitness = fitness(tmpIndividual);
        while (fitness * fitnessCoef > 1)
            fitnessCoef *= 0.1;
        fitnessCoef *= 10;
    }

    private List<Integer> checkAnAddBaseTownToIndividual(List<Integer> individual) {
        var currDemand = 0;
        int i = 0;
        if (individual.get(0) != 0)
            individual.add(0, 0);
        while (i < individual.size()) {
            if (individual.get(i) == 0) {
                i++;
                currDemand = 0;
                continue;
            }
            if (currDemand + points.get(individual.get(i)).demand <= vehicleCapacity) {
                currDemand += points.get(individual.get(i)).demand;
            } else {
                individual.add(i, 0);
                currDemand = 0;
            }
            i++;
        }
        if (individual.get(individual.size() - 1) != 0)
            individual.add(0);
        return individual;
    }

    @Override
    public List<Integer> makeOneIndividual() {
        var newIndividual = new ArrayList<Integer>();
        var shuffledIndividual = new ArrayList<Integer>();
        for (int i = 1; i < points.size(); i++)
            newIndividual.add(i);
        var length = newIndividual.size();
        for (int i = 0; i < length; i++) {
            var item = newIndividual.get(BaseController.rndm.nextInt(newIndividual.size()));
            shuffledIndividual.add(item);
            newIndividual.remove(item);
        }

        return checkAnAddBaseTownToIndividual(shuffledIndividual);
    }

    @Override
    public List<Integer> mutate(List<Integer> individual) {
        var newindividual = individual.stream().filter(e -> e != 0).collect(Collectors.toList());

        var index = BaseController.rndm.nextInt(newindividual.size() - 2) + 1;
        Collections.swap(newindividual, index, index + 1);

        return checkAnAddBaseTownToIndividual(newindividual);
    }

    @Override
    public Double fitness(List<Integer> individual) {
        var currentFitness = 0.0;
        for (int i = 0; i < individual.size() - 1; i++) {
            currentFitness += matrixOfDistances.get(individual.get(i)).get(individual.get(i + 1));
        }

        return (currentFitness) * fitnessCoef ;
    }

    @Override
    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var newparent1 = parent1.stream().filter(e -> e != 0).collect(Collectors.toList());
        var newparent2 = parent2.stream().filter(e -> e != 0).collect(Collectors.toList());
        var child1 = new ArrayList<Integer>();
        var index = BaseController.rndm.nextInt(newparent1.size() - 2) + 1;
        for (int i = 0; i < parent1.size(); i++) {
            if (i < index) {
                child1.add(newparent1.get(i));
                newparent2.remove(newparent1.get(i));
            } else {
                child1.addAll(newparent2);
                break;
            }
        }
        return new Pair<>(checkAnAddBaseTownToIndividual(child1), null);
    }

    @Override
    public String nameForFaces() {
        return "Vehicle Routing Problem";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "VRPPage.fxml";
        return arr;
    }

    @Override
    public void visualize(Canvas canvas, AlgorithmResults data) {
        if (data != null) {
            var gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.web("#F7EDE2"));
            gc.fillRect(-5, -5, canvas.getWidth() + 5, canvas.getHeight() + 5);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(5);
            var LEFT_OFFSET = 170;
            var UP_OFFSET = 5;
            var best = data.getBestIndividual();
            var usedColors = new ArrayList<Color>();

            for (int i = 0; i < best.size() - 1; i++) {
                if (points.get(best.get(i)).demand == 0) {
                        gc.setStroke(colorsOfItems.get(best.get(i+1)));
                        usedColors.add(colorsOfItems.get(best.get(i+1)));
                }
                gc.setLineWidth(2);
                gc.strokeLine(points.get(best.get(i)).xCor  + LEFT_OFFSET + 3,
                        points.get(best.get(i)).yCor + UP_OFFSET + 3,
                        points.get(best.get(i+1)).xCor  + LEFT_OFFSET + 3,
                        points.get(best.get(i+1)).yCor + UP_OFFSET + 3);
            }

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeText("Vehicle capacity: " + this.vehicleCapacity, LEFT_OFFSET + 550, 70);
            gc.strokeText("Trails:", LEFT_OFFSET + 550, 90);
            for (int i = 0; i < usedColors.size(); i++) {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeText(i+"", LEFT_OFFSET + 560, 110 + i*20);
                gc.setStroke(usedColors.get(i));
                gc.setLineWidth(4);
                gc.strokeLine(LEFT_OFFSET + 575, 105+i*20, LEFT_OFFSET + 595, 105+i*20);
            }


            for (int i = 0; i < points.size(); i++) {
                if (i == 0) {
                    gc.setFill(Color.RED);
                    gc.fillRect(points.get(i).xCor + LEFT_OFFSET, points.get(i).yCor + UP_OFFSET, 6, 6);
                    continue;
                }
                gc.setFill(Color.BLACK);
                gc.fillRect(points.get(i).xCor + LEFT_OFFSET, points.get(i).yCor + UP_OFFSET, 6, 6);
                gc.fillText(this.points.get(i).demand+"", points.get(i).xCor + LEFT_OFFSET - 1, points.get(i).yCor + UP_OFFSET - 1);
            }
        }
    }
}
