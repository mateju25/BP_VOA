package controllers.base;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.algorithms.ArtificialBeeColonyAlgorithm;
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
    public Label warning;
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
    public ImageView toolNumberOfItems;
    public ImageView toolCapacity;
    public ImageView toolAverage;
    //vrp
    public TextField sizeOfProblem;
    public TextField vehicleCapacity;
    public TextField averageDemand;
    public ImageView toolSizeProblem;
    public ImageView toolAverageDemand;
    public ImageView toolVehicleCapacity;
    //tap
    public TextField numberOfWeapons;
    public TextField numberOfTargets;
    public TextField maxAssignedTargets;
    public ImageView toolNumberOfWeapons;
    public ImageView toolMaximum;
    public ImageView toolNumberOfTargets;

    public void initialize() {
        if (controllerLoaded)
            return;


        BaseController.rndm = new Random(1);
        controllerLoaded = true;
        if (BaseController.algorithms == null) {
            BaseController.algorithms = new ArrayList<>();
            BaseController.algorithms.add(new AntColonySystemAlgorithm());
            BaseController.algorithms.add(new ArtificialBeeColonyAlgorithm());
            BaseController.algorithms.add(new GeneticAlgorithm());
        }
        if (BaseController.problems == null) {
            BaseController.problems = new ArrayList<>();
            BaseController.problems.add(new VehicleRoutingProblem());
            BaseController.problems.add(new TargetAssignmentProblem());
            BaseController.problems.add(new KnapsackProblem());
        }

        if (algChoiceBox != null) {
            algChoiceBox.getItems().setAll(BaseController.algorithms);
            if (BaseController.chosedAlgorithm != null) {
                var index = BaseController.algorithms.indexOf(BaseController.chosedAlgorithm);
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
            algChoiceBox.getSelectionModel().select(1);

        }
        if (probChoiceBox != null) {
            probChoiceBox.getItems().setAll(BaseController.problems);
            if (BaseController.chosedProblem != null) {
                var index = BaseController.problems.indexOf(BaseController.chosedProblem);
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
            if (BaseController.chosedProblem != null)
                probChoiceBox.getSelectionModel().select(BaseController.problems.indexOf(BaseController.chosedProblem));
            else
                probChoiceBox.getSelectionModel().select(0);
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

        if (averageWeight != null) {
            averageWeight.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            backpackCapacity.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(800));
            numberOfItems.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            BaseController.makeTooltip(toolNumberOfItems,"Number of items that can be put into backpack (each will have a random generated size)");
            BaseController.makeTooltip(toolCapacity,"Maximum load that can be put into backpack");
            BaseController.makeTooltip(toolAverage,"Average size of item that will be put into backpack");
        }
        if (sizeOfProblem != null) {
            sizeOfProblem.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            vehicleCapacity.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            averageDemand.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            BaseController.makeTooltip(toolSizeProblem,"Number of cities that will be generated (each will have a random value of demand)");
            BaseController.makeTooltip(toolAverageDemand,"Average demand for cities");
            BaseController.makeTooltip(toolVehicleCapacity,"Maximum capacity of vehicle (sum of city demands of one trip must not exceed the vehicle capacity)");
        }
        if (numberOfWeapons != null) {
            numberOfWeapons.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(40));
            numberOfTargets.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(40));
            maxAssignedTargets.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(40));
            BaseController.makeTooltip(toolNumberOfWeapons,"Number of weapons that will be generated (each will have a random probability to destroy each target)");
            BaseController.makeTooltip(toolMaximum,"Maximum number of assigned targets to one weapon");
            BaseController.makeTooltip(toolNumberOfTargets,"Number of targets that will be generated (each will have a destruction level 1 - 5)");
        }
    }

    public void proceed(ActionEvent actionEvent) throws IOException {
        if (BaseController.chosedProblem != null && BaseController.chosedAlgorithm != null) {
            if (BaseController.chosedProblem instanceof  KnapsackProblem) {
                ((KnapsackProblem) BaseController.chosedProblem).populateProblem(Integer.valueOf(numberOfItems.getText()),
                        Integer.valueOf(averageWeight.getText()), Integer.valueOf(backpackCapacity.getText()));
            }
            if (BaseController.chosedProblem instanceof  VehicleRoutingProblem) {
                if (Integer.parseInt(averageDemand.getText()) > Integer.parseInt(vehicleCapacity.getText())) {
                    warning.setText("Average demand should not be higher than vehicle capacity!");
                    return;
                }
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

    public void removeWarning(MouseEvent mouseEvent) {
        warning.setText("");
    }

    public void visualizeData(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/vizualizationPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }
}
