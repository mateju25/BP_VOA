package model.problems;

public class VehicleRoutingProblem implements Problem{
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
}
