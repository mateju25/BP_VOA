package model.problems;

public class TargetAssignmentProblem implements Problem{
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
}
