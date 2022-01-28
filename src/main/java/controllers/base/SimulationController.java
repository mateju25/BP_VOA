package controllers.base;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.problems.Problem;

import java.io.IOException;
import java.util.Objects;

public class SimulationController {
    @FXML
    public Label heading;
    @FXML
    public Pane algoPane;

    public void initialize() throws IOException {
        heading.setText(BaseController.chosedAlgorithm.nameForFaces() + " solves " + BaseController.chosedProblem.nameForFaces());
        Parent newPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/parts/" + BaseController.chosedAlgorithm.nameOfFxmlFiles()[0])));
        algoPane.getChildren().add(newPane);
    }

    public void goToMainPage(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }
}
