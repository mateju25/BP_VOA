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
import model.utils.TextFormattersFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MainPageController {
    private Boolean controllerLoaded = false;

    @FXML
    public ChoiceBox<Algorithm> algChoiceBox;
    @FXML
    public ChoiceBox<Problem> probChoiceBox;
    @FXML
    public Pane probPane;
    //knapsack
    public TextField numberOfItems;
    public TextField backpackCapacity;
    public TextField averageWeight;
    //vrp
    public TextField sizeOfProblem;
    public TextField vehicleCapacity;
    public TextField averageDemand;
    //tap
    public TextField numberOfWeapons;
    public TextField numberOfTargets;
    public TextField maxAssignedTargets;

    public void initialize() {
        if (controllerLoaded)
            return;
        BaseController.rndm = new Random(1);
        controllerLoaded = true;
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
            algChoiceBox.getSelectionModel().select(2);

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

                    if (averageWeight != null) {
                        averageWeight.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                        backpackCapacity.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                        numberOfItems.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                    }
                    if (sizeOfProblem != null) {
                        sizeOfProblem.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                        vehicleCapacity.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                        averageDemand.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                    }
                    if (numberOfWeapons != null) {
                        numberOfWeapons.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                        numberOfTargets.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                        maxAssignedTargets.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
                    }
                }
            });
            probChoiceBox.getSelectionModel().select(1);
        }
    }

    private void loadSpecificFxmlPart(String part) {
        Parent newPane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/parts/" + part)));
            loader.setController(this);
            newPane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        probPane.getChildren().add(newPane);
    }

    public void proceed(ActionEvent actionEvent) throws IOException {
        if (BaseController.chosedProblem != null && BaseController.chosedAlgorithm != null) {
            if (BaseController.chosedProblem instanceof  KnapsackProblem) {
                ((KnapsackProblem) BaseController.chosedProblem).populateProblem(Integer.valueOf(numberOfItems.getText()),
                        Integer.valueOf(averageWeight.getText()), Integer.valueOf(backpackCapacity.getText()));
            }
            if (BaseController.chosedProblem instanceof  VehicleRoutingProblem) {
                ((VehicleRoutingProblem) BaseController.chosedProblem).populateProblem(Integer.valueOf(sizeOfProblem.getText()),
                        Integer.valueOf(vehicleCapacity.getText()), Integer.valueOf(averageDemand.getText()));
            }
            if (BaseController.chosedProblem instanceof  TargetAssignmentProblem) {
                ((TargetAssignmentProblem) BaseController.chosedProblem).populateProblem(Integer.valueOf(numberOfTargets.getText()),
                        Integer.valueOf(numberOfWeapons.getText()), Integer.valueOf(maxAssignedTargets.getText()));
            }
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/algorithmPage.fxml")));
            BaseController.mainStage.setScene(new Scene(root));
            BaseController.mainStage.show();
        }
    }
}
