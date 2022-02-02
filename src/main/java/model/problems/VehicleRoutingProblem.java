package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;
import model.utils.AlgorithmResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleRoutingProblem implements Problem {
    @Getter
    private class Point {
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
    private List<List<Double>> matrixOfDistances;
    private Integer vehicleCapacity;
    private Integer fitnessCoef;

    public void populateProblem(Integer sizeOfTheProblem, Integer vehicleCapacity, Integer averageDemand) {
        points = new ArrayList<>();
        points.add(new Point(100, 100, 0));
        for (int i = 0; i < sizeOfTheProblem - 1; i++) {
            var demand = BaseController.rndm.nextInt(2*averageDemand - 2) + 1;
            while (demand > vehicleCapacity)
                demand = BaseController.rndm.nextInt(2*averageDemand - 2) + 1;
            var tmp = new Point(BaseController.rndm.nextInt(200), BaseController.rndm.nextInt(200), demand);
            points.add(tmp);
        }
        matrixOfDistances = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            matrixOfDistances.add(new ArrayList<>(points.size()));
            for (int j = 0; j < points.size(); j++) {
                matrixOfDistances.get(i).add(j, 0.0);
            }
        }
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                var distance = Math.sqrt(Math.pow(Math.abs(points.get(i).xCor - points.get(j).xCor), 2) + Math.pow(Math.abs(points.get(i).yCor - points.get(j).yCor), 2));
                matrixOfDistances.get(i).set(j, distance);
                matrixOfDistances.get(j).set(i, distance);
            }
        }
        this.vehicleCapacity = vehicleCapacity;

        var tmpIndividual = makeOneIndividual();
        fitnessCoef = 1;
        var fitness = fitness(tmpIndividual);
        while (fitness * fitnessCoef < 1)
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
                i++;
            } else {
                individual.add(i, 0);
                currDemand = 0;
                i++;
            }
        }
        if (individual.get(individual.size() - 1) != 0)
            individual.add(0);
        return individual;
    }

    @Override
    public List<Integer> makeOneIndividual() {
        var newIndividual = new ArrayList<Integer>();
        for (int i = 1; i < points.size(); i++)
            newIndividual.add(i);
        Collections.shuffle(newIndividual);

        return checkAnAddBaseTownToIndividual(newIndividual);
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

        return (1/currentFitness * fitnessCoef) ;
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
            var HEIGHT_OF_CONTAINER = 445;
            var best = data.getBestIndividual();
            var level = 0;

            for (int i = 0; i < best.size() - 1; i++) {
                if (points.get(best.get(i)).demand == 0)
                    gc.setStroke(new Color(BaseController.rndm.nextFloat(), BaseController.rndm.nextFloat(), BaseController.rndm.nextFloat(), 1));
                gc.setLineWidth(1);
                gc.strokeLine(points.get(best.get(i)).xCor  + 400 + 3,
                        points.get(best.get(i)).yCor + 112 + 3,
                        points.get(best.get(i+1)).xCor  + 400 + 3,
                        points.get(best.get(i+1)).yCor + 112 + 3);
            }
            for (int i = 0; i < points.size(); i++) {
                if (i == 0) {
                    gc.setFill(Color.RED);
                    gc.fillRect(points.get(i).xCor + 400, points.get(i).yCor + 112, 6, 6);
                    continue;
                }
                gc.setFill(Color.BLACK);
                gc.fillRect(points.get(i).xCor + 400, points.get(i).yCor + 112, 6, 6);
            }
        }
    }
}
