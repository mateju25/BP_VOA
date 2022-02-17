package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.utils.AlgorithmResults;
import model.utils.DistinctColors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private Integer sizeOfTheProblem;
    private Double fitnessCoefficient;
    private List<Color> colorsOfItems;


    @Override
    public void init(Map<String, String> parameters) {
        this.sizeOfTheProblem = Integer.parseInt(parameters.get("sizeOfProblem"));
        this.vehicleCapacity = Integer.parseInt(parameters.get("vehicleCapacity"));
        var averageDemand = Integer.parseInt(parameters.get("averageDemand"));

        points = new ArrayList<>();
        colorsOfItems = new ArrayList<>();
        points.add(new Point(250, 250, 0));
        for (int i = 0; i < sizeOfTheProblem - 1; i++) {
            var demand = BaseController.randomGenerator.nextInt(2*averageDemand - 2) + 1;
            while (demand > vehicleCapacity)
                demand = BaseController.randomGenerator.nextInt(2*averageDemand - 2) + 1;
            var tmp = new Point(BaseController.randomGenerator.nextInt(500), BaseController.randomGenerator.nextInt(500), demand);
            points.add(tmp);
        }
        for (int i = 0; i < sizeOfTheProblem; i++) {
            var item = Color.web(DistinctColors.colors[BaseController.randomGenerator.nextInt(DistinctColors.colors.length)]);
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

        var tmpIndividual = makeOneIndividual();
        fitnessCoefficient = 1.0;
        var fitness = fitness(tmpIndividual);
        while (fitness * fitnessCoefficient > 1)
            fitnessCoefficient *= 0.1;
        fitnessCoefficient *= 10;
    }

    @Override
    public List<List<Double>> initPheromoneMatrix() {
        ArrayList<List<Double>> matrix = new ArrayList<>();
        for (int i = 0; i < sizeOfTheProblem; i++) {
            matrix.add(new ArrayList<>(Collections.nCopies(sizeOfTheProblem, 0.0)));
//            matrix.add(new ArrayList<>(matrixOfDistances.get(i).stream().mapToDouble(Double::valueOf).boxed().collect(Collectors.toList())));
        }
        return matrix;
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
            var item = newIndividual.get(BaseController.randomGenerator.nextInt(newIndividual.size()));
            shuffledIndividual.add(item);
            newIndividual.remove(item);
        }

        return checkAnAddBaseTownToIndividual(shuffledIndividual);
    }

    public List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs) {
        var newIndividual = new ArrayList<Integer>();
        newIndividual.add(0);
        Integer fromCity = 0;
        var needToVisitPlaces = IntStream.rangeClosed(1, sizeOfTheProblem-1).boxed().collect(Collectors.toList());
        while (needToVisitPlaces.size() > 0) {
            var probList = new ArrayList<Double>();
            for (int i = 0; i < needToVisitPlaces.size(); i++) {
                probList.add(acs.getProbabilityOfEdgeToSelect(fromCity, needToVisitPlaces.get(i), new ArrayList<>(needToVisitPlaces)));
            }

            var index = Algorithm.getCumulativeFitnessesIndex(probList);

            Integer toCity = needToVisitPlaces.remove(index);
            newIndividual.add(toCity);

            acs.getMatrixOfPheromone().get(fromCity).set(toCity,
                    acs.newProbValueDueToValue(fromCity, toCity, matrixOfDistances.get(fromCity).stream().filter(e -> e != 0).mapToDouble(Double::valueOf).boxed().collect(Collectors.toList())));

            fromCity = toCity;
        }
        return checkAnAddBaseTownToIndividual(newIndividual);
    }

    @Override
    public Double getHeuristicValue(Integer from, Integer to) {
        return matrixOfDistances.get(from).get(to) + 0.0;
    }


    @Override
    public List<Integer> mutate(List<Integer> individual) {
        var newIndividual = individual.stream().filter(e -> e != 0).collect(Collectors.toList());

        var index = BaseController.randomGenerator.nextInt(newIndividual.size() - 2) + 1;
        Collections.swap(newIndividual, index, index + 1);

        return checkAnAddBaseTownToIndividual(newIndividual);
    }

    @Override
    public Double fitness(List<Integer> individual) {
        var currentFitness = 0.0;
        for (int i = 0; i < individual.size() - 1; i++) {
            currentFitness += matrixOfDistances.get(individual.get(i)).get(individual.get(i + 1));
        }

        return (currentFitness) * fitnessCoefficient;
    }

    @Override
    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var newParent1 = parent1.stream().filter(e -> e != 0).collect(Collectors.toList());
        var newParent2 = parent2.stream().filter(e -> e != 0).collect(Collectors.toList());
        var child1 = new ArrayList<Integer>();
        var index = BaseController.randomGenerator.nextInt(newParent1.size() - 2) + 1;
        for (int i = 0; i < parent1.size(); i++) {
            if (i < index) {
                child1.add(newParent1.get(i));
                newParent2.remove(newParent1.get(i));
            } else {
                child1.addAll(newParent2);
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
