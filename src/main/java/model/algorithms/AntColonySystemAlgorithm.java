package model.algorithms;

import lombok.Getter;
import lombok.Setter;
import model.problems.Problem;
import model.utils.AlgorithmResults;

@Getter @Setter
public class AntColonySystemAlgorithm implements Algorithm {
    private Problem problem;

    @Override
    public void initFirstGeneration() {

    }

    @Override
    public AlgorithmResults nextGeneration() {
        return null;
    }

    @Override
    public void resetAlgorithm() {

    }

    @Override
    public String nameForFaces() {
        return "Ant Colony System Algorithm";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "ACSPage.fxml";
        return arr;
    }
}
