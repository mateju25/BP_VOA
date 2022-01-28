package controllers.base;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.algorithms.ArtifialBeeColonyAlgorithm;
import model.algorithms.GeneticAlgorithm;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.problems.TargetAssignmentProblem;
import model.problems.VehicleRoutingProblem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainPageController {
    @FXML
    public ChoiceBox<Algorithm> algChoiceBox;
    @FXML
    public ChoiceBox<Problem> probChoiceBox;
    @FXML
    public Pane probPane;

    public void initialize() {
        var algorithms = new ArrayList<Algorithm>();
        algorithms.add(new AntColonySystemAlgorithm());
        algorithms.add(new ArtifialBeeColonyAlgorithm());
        algorithms.add(new GeneticAlgorithm());
        var problems = new ArrayList<Problem>();
        problems.add(new VehicleRoutingProblem());
        problems.add(new TargetAssignmentProblem());
        problems.add(new KnapsackProblem());
        if (algChoiceBox != null) {
            algChoiceBox.getItems().setAll(algorithms);
            if (BaseController.chosedAlgorithm != null) {
                var index = algorithms.indexOf(algorithms.stream().filter(e -> e.getClass().getName().equals(BaseController.chosedAlgorithm.getClass().getName())).findFirst().get());
                algChoiceBox.getSelectionModel().clearAndSelect(index);
            }
            algChoiceBox.setConverter(
                    new StringConverter<>() {
                        @Override
                        public String toString(Algorithm object) {
                            return object.nameForFaces();
                        }

                        @Override
                        public Algorithm fromString(String string) {
                            return null;
                        }
                    });
            algChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> BaseController.chosedAlgorithm = algChoiceBox.getItems().get(newValue.intValue()));
        }
        if (probChoiceBox != null) {
            probChoiceBox.getItems().setAll(problems);
            if (BaseController.chosedProblem != null) {
                var index = problems.indexOf(problems.stream().filter(e -> e.getClass().getName().equals(BaseController.chosedProblem.getClass().getName())).findFirst().get());
                probChoiceBox.getSelectionModel().clearAndSelect(index);
                Problem selectedProblem = probChoiceBox.getItems().get(index);
                loadSpecificFxmlPart(selectedProblem.nameOfFxmlFiles()[0]);
            }
            probChoiceBox.setConverter(
                    new StringConverter<>() {
                        @Override
                        public String toString(Problem object) {
                            return object.nameForFaces();
                        }

                        @Override
                        public Problem fromString(String string) {
                            return null;
                        }
                    });
            probChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
                @SneakyThrows
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    Problem selectedProblem = probChoiceBox.getItems().get(newValue.intValue());
                    loadSpecificFxmlPart(selectedProblem.nameOfFxmlFiles()[0]);
                    BaseController.chosedProblem = selectedProblem;
                }
            });
        }


    }

    private void loadSpecificFxmlPart(String part) {
        Parent newPane = null;
        try {
            newPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/parts/" + part)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        probPane.getChildren().add(newPane);
    }

    public void proceed(ActionEvent actionEvent) throws IOException {
        if (BaseController.chosedProblem != null && BaseController.chosedAlgorithm != null) {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/simulationPage.fxml")));
            BaseController.mainStage.setScene(new Scene(root));
            BaseController.mainStage.show();
        }
    }
}
