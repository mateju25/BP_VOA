package controllers.base;

import controllers.components.MenuController;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import model.algorithms.Algorithm;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.problems.TargetAssignmentProblem;
import model.problems.VehicleRoutingProblem;
import model.utils.TextFormattersFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class MainPageController extends MenuController {
    @FXML public Label warning;
    @FXML public ImageView animationPic;
    @FXML public ChoiceBox<String> presetProblems;
    @FXML public ChoiceBox<Problem> probChoiceBox;
    @FXML public Pane probPane;

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

    private Boolean controllerLoaded = false;

    public void initialize() {
        if (controllerLoaded)
            return;
        controllerLoaded = true;
        speedChangerMenu.adjustValue(BaseController.simulationSpeed);

        BaseController.simulationSpeed = 0;
        initMenu();
        makeAnimation();

        BaseController.init();


        if (probChoiceBox != null) {
            probChoiceBox.getItems().setAll(BaseController.problems);
            if (BaseController.chosenProblem != null) {
                var index = BaseController.problems.indexOf(BaseController.chosenProblem);
                probChoiceBox.getSelectionModel().clearAndSelect(index);
                Problem selectedProblem = probChoiceBox.getItems().get(index);
                loadSpecificFxmlPart(selectedProblem.nameOfFxmlFiles());
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
                    BaseController.isProblemGenerated = false;
                    Problem selectedProblem = probChoiceBox.getItems().get(newValue.intValue());
                    loadSpecificFxmlPart(selectedProblem.nameOfFxmlFiles());
                    BaseController.chosenProblem = selectedProblem;

                    presetProblems.getItems().clear();
                    for (Integer number : BaseController.chosenProblem.presetProblems()) {
                        presetProblems.getItems().add("Preset " + BaseController.chosenProblem.nameForFaces() + " " + number);
                    }
                }
            });
            if (BaseController.chosenProblem != null) {
                probChoiceBox.getSelectionModel().select(BaseController.problems.indexOf(BaseController.chosenProblem));
                for (Integer number : BaseController.chosenProblem.presetProblems()) {
                    presetProblems.getItems().add("Preset " + BaseController.chosenProblem.nameForFaces() + " " + number);
                }
            } else
                probChoiceBox.getSelectionModel().select(0);
        }

        presetProblems.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (BaseController.chosenProblem != null && !newValue.equals(-1)) {
                BaseController.chosenProblem.setPreset((Integer) newValue);
                actualizeTextEdits();
            }
        });
    }

    private void makeAnimation() {
        if (BaseController.isFirstLoad) {
            BaseController.isFirstLoad = false;
            animationPic.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon_animation.png"))));
            FadeTransition ft = new FadeTransition(Duration.millis(1500), animationPic);
            ft.setDelay(Duration.millis(2500));
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            ft.setOnFinished(event -> animationPic.setMouseTransparent(true));

        }
        animationPic.setMouseTransparent(true);

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
            BaseController.makeTooltip(toolNumberOfItems, "Number of items that can be put into backpack (each will have a random generated size)");
            BaseController.makeTooltip(toolCapacity, "Maximum load that can be put into backpack (max number allowed - 800)");
            BaseController.makeTooltip(toolAverage, "Average size of item that will be put into backpack");
        }
        if (sizeOfProblem != null) {
            sizeOfProblem.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            vehicleCapacity.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            averageDemand.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            BaseController.makeTooltip(toolSizeProblem, "Number of cities that will be generated (each will have a random value of demand)");
            BaseController.makeTooltip(toolAverageDemand, "Average demand for cities");
            BaseController.makeTooltip(toolVehicleCapacity, "Maximum capacity of vehicle (sum of city demands of one trip must not exceed the vehicle capacity)");
        }
        if (numberOfWeapons != null) {
            numberOfWeapons.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(40));
            numberOfTargets.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(40));
            maxAssignedTargets.setTextFormatter(TextFormattersFactory.makeIntegerFormatter(40));
            BaseController.makeTooltip(toolNumberOfWeapons, "Number of weapons that will be generated (each will have a random probability to destroy each target) (max number allowed - 40)");
            BaseController.makeTooltip(toolMaximum, "Maximum number of assigned targets to one weapon (max number allowed - 40)");
            BaseController.makeTooltip(toolNumberOfTargets, "Number of targets that will be generated (each will have a destruction level 1 - 5) (max number allowed - 40)");
        }
        actualizeTextEdits();
    }

    private void actualizeTextEdits() {
        if (averageWeight != null) {
            if (BaseController.chosenProblem instanceof KnapsackProblem) {
                averageWeight.setText(((KnapsackProblem) BaseController.chosenProblem).getAverageWeightOfItem() + "");
                backpackCapacity.setText(((KnapsackProblem) BaseController.chosenProblem).getWeightOfBackpack() + "");
                numberOfItems.setText(((KnapsackProblem) BaseController.chosenProblem).getNumberOfItems() + "");
            }
        }
        if (sizeOfProblem != null) {
            if (BaseController.chosenProblem instanceof VehicleRoutingProblem) {
                sizeOfProblem.setText(((VehicleRoutingProblem) BaseController.chosenProblem).getSizeOfTheProblem() + "");
                vehicleCapacity.setText(((VehicleRoutingProblem) BaseController.chosenProblem).getVehicleCapacity() + "");
                averageDemand.setText(((VehicleRoutingProblem) BaseController.chosenProblem).getAverageDemand() + "");
            }
        }
        if (numberOfWeapons != null) {
            if (BaseController.chosenProblem instanceof TargetAssignmentProblem) {
                numberOfWeapons.setText(((TargetAssignmentProblem) BaseController.chosenProblem).getNumOfWeapons() + "");
                numberOfTargets.setText(((TargetAssignmentProblem) BaseController.chosenProblem).getNumOfTargets() + "");
                maxAssignedTargets.setText(((TargetAssignmentProblem) BaseController.chosenProblem).getMaximumAssignedTargets() + "");
            }
        }
    }

    public void proceed() throws IOException {
        if (BaseController.chosenProblem != null) {
            if (averageDemand != null && vehicleCapacity != null && Integer.parseInt(averageDemand.getText()) > Integer.parseInt(vehicleCapacity.getText())) {
                warning.setText("Average demand should not be higher than vehicle capacity!");
                return;
            }

            if (averageWeight != null && Integer.parseInt(averageWeight.getText()) == 0) {
                warning.setText("Average weight should not be zero!");
                return;
            }

            if (averageDemand != null && Integer.parseInt(averageDemand.getText()) == 0) {
                warning.setText("Average demand should not be zero!");
                return;
            }



            var map = new HashMap<String, String>();
            if (numberOfItems != null) map.put(numberOfItems.getId(), numberOfItems.getText());
            if (averageWeight != null) map.put(averageWeight.getId(), averageWeight.getText());
            if (backpackCapacity != null) map.put(backpackCapacity.getId(), backpackCapacity.getText());
            if (sizeOfProblem != null) map.put(sizeOfProblem.getId(), sizeOfProblem.getText());
            if (vehicleCapacity != null) map.put(vehicleCapacity.getId(), vehicleCapacity.getText());
            if (averageDemand != null) map.put(averageDemand.getId(), averageDemand.getText());
            if (numberOfTargets != null) map.put(numberOfTargets.getId(), numberOfTargets.getText());
            if (numberOfWeapons != null) map.put(numberOfWeapons.getId(), numberOfWeapons.getText());
            if (maxAssignedTargets != null) map.put(maxAssignedTargets.getId(), maxAssignedTargets.getText());

            BaseController.chosenProblem.init(map);
            BaseController.chosenProblem.regenerate();


            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/algorithmPage.fxml")));
            BaseController.mainStage.setScene(new Scene(root));
            BaseController.mainStage.show();
        } else {
            warning.setText("Please select a problem!");
        }
    }

    public void removeWarning() {
        warning.setText("");
    }

    public void visualizeData() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/visualizationPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

}
