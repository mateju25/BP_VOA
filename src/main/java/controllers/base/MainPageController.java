package controllers.base;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.algorithms.ArtifialBeeColonyAlgorithm;
import model.algorithms.GeneticAlgorithm;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.problems.TargetAssignmentProblem;
import model.problems.VehicleRoutingProblem;

import java.util.ArrayList;

public class MainPageController {
    @FXML
    public ChoiceBox<Algorithm> algChoiceBox;
    @FXML
    public ChoiceBox<Problem>  probChoiceBox;

    public void initialize() {
        var algorithms = new ArrayList<Algorithm>();
        algorithms.add(new AntColonySystemAlgorithm());
        algorithms.add(new ArtifialBeeColonyAlgorithm());
        algorithms.add(new GeneticAlgorithm());
        var problems = new ArrayList<Problem>();
        problems.add(new VehicleRoutingProblem());
        problems.add(new TargetAssignmentProblem());
        problems.add(new KnapsackProblem());
        algChoiceBox.getItems().setAll(algorithms);
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
        algChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println(algChoiceBox.getItems().get(newValue.intValue()).nameForFaces());
            }
        });
        probChoiceBox.getItems().setAll(problems);
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
        probChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println(probChoiceBox.getItems().get(newValue.intValue()).nameForFaces());
            }
        });
    }
}
