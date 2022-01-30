package model.algorithms;

public class AntColonySystemAlgorithm implements Algorithm {
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
