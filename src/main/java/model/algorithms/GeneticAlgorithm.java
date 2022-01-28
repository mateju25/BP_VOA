package model.algorithms;

public class GeneticAlgorithm implements Algorithm{
    @Override
    public String nameForFaces() {
        return "Genetic Algorithm";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "GAPage.fxml";
        return arr;
    }
}
