package controllers.base;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.algorithms.GeneticAlgorithm;
import model.problems.KnapsackProblem;
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

        GeneticAlgorithm ga = (GeneticAlgorithm) BaseController.chosedAlgorithm;
        KnapsackProblem kp = (KnapsackProblem) BaseController.chosedProblem;
        kp.populateProblem(100, 6, 500);
        ga.setAlgorithm(100, 100, 0.0,0.0,0,0.5,0.5);
        ga.initFirstGeneration(kp);
        for (int i = 0; i < 100; i++) {
            var res = ga.nextGeneration(kp);
            System.out.println(res.toString(kp));
        }
    }

    public void goToMainPage(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }
}
