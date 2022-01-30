package model.algorithms;

public class ArtifialBeeColonyAlgorithm implements Algorithm{
    @Override
    public void resetAlgorithm() {

    }
    @Override
    public String nameForFaces() {
        return "Artificial Bee Colony Algorithm";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "ABCPage.fxml";
        return arr;
    }
}
