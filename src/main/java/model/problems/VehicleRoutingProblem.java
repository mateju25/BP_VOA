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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of vehicle routing problem
 */
@Getter
@Setter
public class VehicleRoutingProblem implements Problem {
    /**
     * Private class that represents city.
     */
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
    @JsonIgnore
    private List<Point> points;
    @JsonIgnore
    private List<List<Integer>> matrixOfDistances;
    private Integer vehicleCapacity;
    private Integer sizeOfTheProblem;
    private Integer averageDemand;
    @JsonIgnore
    private Double fitnessCoefficient;
    @JsonIgnore
    private List<Color> colorsOfItems;

    /**
     * Initializes problem with parameters.
     * @param parameters parameters from input fields.
     */
    @Override
    public void init(Map<String, String> parameters) {
        this.sizeOfTheProblem = Integer.parseInt(parameters.get("sizeOfProblem"));
        this.vehicleCapacity = Integer.parseInt(parameters.get("vehicleCapacity"));
        this.averageDemand = Integer.parseInt(parameters.get("averageDemand"));
    }

    /**
     * Regenerates problem. Creates cities and distance matrix.
     */
    @Override
    public void regenerate() {
        points = new ArrayList<>();
        colorsOfItems = new ArrayList<>();
        points.add(new Point(250, 250, 0));
        for (int i = 0; i < sizeOfTheProblem - 1; i++) {
            var demand = BaseController.randomGenerator.nextInt(2 * averageDemand - 1) + 1;
            while (demand > vehicleCapacity)
                demand = BaseController.randomGenerator.nextInt(2 * averageDemand - 1) + 1;
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

    /**
     * Initializes matrix of pheromones with dimension of the problem, used in ACS
     * @return matrix of pheromones
     */
    @Override
    public List<List<Double>> initPheromoneMatrix() {
        ArrayList<List<Double>> matrix = new ArrayList<>();
        for (int i = 0; i < sizeOfTheProblem; i++) {
            matrix.add(new ArrayList<>(Collections.nCopies(sizeOfTheProblem, 0.0)));
        }
        return matrix;
    }

    /**
     * Check if all constraints are met in VRP.
     * @param individual one solution of the problem
     * @return sanitized solution
     */
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

    /**
     * Make one solution of the problem, used in GA, ABC
     * @return individual
     */
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

    /**
     * Make one solution of the problem, used in ACS
     * @param acs ACS algorithm instance
     * @return individual
     */
    @Override
    public List<Integer> makeOneIndividual(AntColonySystemAlgorithm acs) {
        var newIndividual = new ArrayList<Integer>();
        newIndividual.add(0);
        Integer fromCity = 0;
        var currDemand = 0;
        var needToVisitPlaces = IntStream.rangeClosed(1, sizeOfTheProblem - 1).boxed().collect(Collectors.toList());
        while (needToVisitPlaces.size() > 0) {
            var probabilities = acs.getProbabilityOfEdges(fromCity, needToVisitPlaces);
            var probList = probabilities.values().stream().mapToDouble(e -> e).boxed().collect(Collectors.toList());

            int index = Algorithm.getCumulativeFitnessesIndex(probList);

            Integer newIndex = 0;
            for (Integer key : probabilities.keySet()) {
                if (probabilities.get(key).equals(probList.get(index)))
                    newIndex = key;
            }

            if (currDemand + points.get(newIndex).demand <= vehicleCapacity) {
                currDemand += points.get(newIndex).demand;

                needToVisitPlaces.remove(newIndex);
                newIndividual.add(newIndex);

                acs.localUpdateEdge(fromCity, newIndex);
                acs.localUpdateEdge(newIndex, fromCity);

                fromCity = newIndex;
            } else {
                newIndividual.add(0);
                currDemand = 0;
            }
        }
        newIndividual.add(0);
        return checkAnAddBaseTownToIndividual(newIndividual);
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
        for (int i = 0; i < individual.size() - 1; i++) {
            edges.get(individual.get(i)).set(individual.get(i + 1), fitness);
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
        return 1 / (matrixOfDistances.get(from).get(to) + 0.001);
    }

    /**
     * @param individual solution of the problem
     * @return fitness value of solution
     */
    @Override
    public Double fitness(List<Integer> individual) {
        var currentFitness = 0.0;
        for (int i = 0; i < individual.size() - 1; i++) {
            currentFitness += matrixOfDistances.get(individual.get(i)).get(individual.get(i + 1));
        }

        return (currentFitness) * fitnessCoefficient;
    }

    /**
     * Changes individual, used in GA
     * @param individual solution of the problem
     * @return mutated individual
     */
    @Override
    public List<Integer> mutate(List<Integer> individual) {
        var newIndividual = individual.stream().filter(e -> e != 0).collect(Collectors.toList());

        var index = BaseController.randomGenerator.nextInt(newIndividual.size() - 1);
        Collections.swap(newIndividual, index, index + 1);

        return checkAnAddBaseTownToIndividual(newIndividual);
    }

    /**
     * Changes individual, used in ABC
     * @param individual one solution of the problem
     * @param probChange strength of the mutation
     * @return changed individual
     */
    @Override
    public List<Integer> localSearch(List<Integer> individual, Double probChange) {
        List<Integer> newIndividual = new ArrayList<>();
        for (int i = 0; i < sizeOfTheProblem * probChange; i++) {
            newIndividual = individual.stream().filter(e -> e != 0).collect(Collectors.toList());

            var index = BaseController.randomGenerator.nextInt(newIndividual.size() - 2) + 1;
            Collections.swap(newIndividual, index, index + 1);
        }


        return checkAnAddBaseTownToIndividual(newIndividual);
    }

    /**
     * Creates two children by single point cross-overing parents, used in GA
     * @param parent1 one solution of the problem
     * @param parent2 second solution of the problem
     * @return pair of children
     */
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

    /**
     * Creates two children by double point cross-overing parents, used in GA
     * @param parent1 one solution of the problem
     * @param parent2 second solution of the problem
     * @return pair of children
     */
    @Override
    public Pair<List<Integer>, List<Integer>> doubleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var newParent1 = parent1.stream().filter(e -> e != 0).collect(Collectors.toList());
        var newParent2 = parent2.stream().filter(e -> e != 0).collect(Collectors.toList());
        var child1 = new ArrayList<Integer>();
        var index = BaseController.randomGenerator.nextInt(newParent1.size() - 2) + 1;
        var indexSecond = BaseController.randomGenerator.nextInt(newParent1.size() - index) + index;
        for (int i = 0; i < newParent1.size(); i++) {
            if (i < index || i > indexSecond) {
                child1.add(newParent1.get(i));
                newParent2.remove(newParent1.get(i));
            }
        }
        child1.addAll(newParent2);
        return new Pair<>(checkAnAddBaseTownToIndividual(child1), null);
    }

    /**
     * @return message that will be displayed in simulation.
     */
    @Override
    public String nameForFaces() {
        return "Vehicle Routing Problem";
    }

    /**
     * @return component that will controller need.
     */
    @Override
    public String nameOfFxmlFiles() {
        return "VRPPage.fxml";
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
            var LEFT_OFFSET = 170;
            var UP_OFFSET = 5;
            var best = data.getBestIndividual();
            var usedColors = new ArrayList<Color>();

            for (int i = 0; i < best.size() - 1; i++) {
                var strokeBefore = gc.getStroke();
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2.2);
                gc.strokeLine(points.get(best.get(i)).xCor + LEFT_OFFSET + 3,
                        points.get(best.get(i)).yCor + UP_OFFSET + 3,
                        points.get(best.get(i + 1)).xCor + LEFT_OFFSET + 3,
                        points.get(best.get(i + 1)).yCor + UP_OFFSET + 3);
                gc.setStroke(strokeBefore);

                if (points.get(best.get(i)).demand == 0) {
                    gc.setStroke(colorsOfItems.get(best.get(i + 1)));
                    usedColors.add(colorsOfItems.get(best.get(i + 1)));
                }
                gc.setLineWidth(2);
                gc.strokeLine(points.get(best.get(i)).xCor + LEFT_OFFSET + 3,
                        points.get(best.get(i)).yCor + UP_OFFSET + 3,
                        points.get(best.get(i + 1)).xCor + LEFT_OFFSET + 3,
                        points.get(best.get(i + 1)).yCor + UP_OFFSET + 3);

            }

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeText("Vehicle capacity: " + this.vehicleCapacity, LEFT_OFFSET + 550, 70);
            gc.strokeText("Trails: " + usedColors.size(), LEFT_OFFSET + 550, 90);

            var offsetX = 0;
            for (int i = 0; i < usedColors.size(); i++) {
                if (i == 79) {
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1);
                    gc.strokeText("and more...", LEFT_OFFSET + 560 + offsetX * 80, 110 + (i % 20) * 20);
                    break;
                }
                offsetX = i / 20;
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeText(i + "", LEFT_OFFSET + 560 + offsetX*80, 110 + (i % 20) * 20);
                gc.setStroke(usedColors.get(i));

                var strokeBefore = gc.getStroke();
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(4.2);
                gc.strokeLine(LEFT_OFFSET + 590 + offsetX*80, 105 + (i % 20) * 20, LEFT_OFFSET + 610 + offsetX*80, 105 + (i % 20) * 20);
                gc.setStroke(strokeBefore);

                gc.setLineWidth(4);
                gc.strokeLine(LEFT_OFFSET + 590  + offsetX*80, 105 + (i % 20) * 20, LEFT_OFFSET + 610 + offsetX*80, 105 + (i % 20) * 20);


            }


            for (int i = 0; i < points.size(); i++) {
                if (i == 0) {
                    gc.setFill(Color.RED);
                    gc.fillRect(points.get(i).xCor + LEFT_OFFSET, points.get(i).yCor + UP_OFFSET, 6, 6);
                    continue;
                }
                gc.setFill(Color.BLACK);
                gc.fillRect(points.get(i).xCor + LEFT_OFFSET, points.get(i).yCor + UP_OFFSET, 6, 6);
                gc.fillText(this.points.get(i).demand + "", points.get(i).xCor + LEFT_OFFSET - 1, points.get(i).yCor + UP_OFFSET - 1);
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
                params.put("sizeOfProblem", "10");
                params.put("vehicleCapacity", "10");
                params.put("averageDemand", "2");
                break;
            }
            case 1: {
                params.put("sizeOfProblem", "20");
                params.put("vehicleCapacity", "20");
                params.put("averageDemand", "3");
                break;
            }
            case 2: {
                params.put("sizeOfProblem", "40");
                params.put("vehicleCapacity", "40");
                params.put("averageDemand", "4");
                break;
            }
        }
        init(params);
    }
}
