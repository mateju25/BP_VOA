package model.problems;

import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import model.utils.AlgorithmResults;

import java.util.List;

public class TargetAssignmentProblem implements Problem{
    public List<Integer> makeOneIndividual() {
        return null;
    }

    @Override
    public Double fitness(List<Integer> individual) {
        return null;
    }

    @Override
    public List<Integer> mutate(List<Integer> individual) {
        return null;
    }

    @Override
    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        return null;
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
