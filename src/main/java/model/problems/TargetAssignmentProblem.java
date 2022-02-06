package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import model.utils.AlgorithmResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TargetAssignmentProblem implements Problem{
    private List<List<Double>> matrixOfProbabilities;
    private Integer maximumAssignedTargets;
    private Integer numOfTargets;
    private Integer numOfWeapons;

    public void populateProblem(Integer numOfTargets, Integer numOfWeapons, Integer maximumAssignedTargets) {
        matrixOfProbabilities = new ArrayList<>(numOfWeapons);
        for (int i = 0; i < numOfWeapons; i++) {
            matrixOfProbabilities.add(new ArrayList<>(numOfTargets));
            for (int j = 0; j < numOfTargets; j++) {
                matrixOfProbabilities.get(i).add(j, BaseController.rndm.nextDouble());
            }
        }
        this.maximumAssignedTargets = maximumAssignedTargets;
        this.numOfWeapons = numOfWeapons;
        this.numOfTargets = numOfTargets;
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
        var currentFitness = 1.0;
        for (int i = 0; i < individual.size() - 1; i+=2) {
            currentFitness *= matrixOfProbabilities.get(individual.get(i)).get(individual.get(i + 1));
        }

        return (1 - currentFitness) ;
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
                child1.addAll(parent2);
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

    }
}
