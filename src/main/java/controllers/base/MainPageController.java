package controllers.base;

import controllers.components.MenuController;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Controller that controls main page and problem generation.
 */
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

    /**
     * Initialize main page view. Initialize menu and all input fields if necessary. Provides animation for startup of application. Populates problem choice box.
     */
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

    /**
     * Makes animation for startup.
     */
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

    /**
     * Loads specific fxml file into page a treats it as component view. Generates constraints on input fields and add tooltips.
     * @param part Path to fxml component file.
     */
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

    /**
     * Populates input fields if problem was already chosen.
     */
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

    /**
     * Puts property into map
     * @param map parameters map
     * @param field input field with value
     */
    private void putIntoMap(Map<String, String> map, TextField field) {
        if (field != null && !field.getText().equals("")) map.put(field.getId(), field.getText());
    }

    /**
     * Check if constrains on problem parameters are met, initializes problem and loads algorithm page.
     * @throws IOException
     */
    public void proceed() throws IOException {
        if (BaseController.chosenProblem != null) {
            var map = new HashMap<String, String>();
            putIntoMap(map, numberOfItems);
            putIntoMap(map, averageWeight);
            putIntoMap(map, backpackCapacity);
            putIntoMap(map, sizeOfProblem);
            putIntoMap(map, vehicleCapacity);
            putIntoMap(map, averageDemand);
            putIntoMap(map, numberOfTargets);
            putIntoMap(map, numberOfWeapons);
            putIntoMap(map, maxAssignedTargets);

            if (map.keySet().size() != 3) {
                warning.setText("No input field should be empty!");
                return;
            }

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

            if (sizeOfProblem != null && Integer.parseInt(sizeOfProblem.getText()) <= 1) {
                warning.setText("Size of the problem should be more than 1!");
                return;
            }



            BaseController.chosenProblem.init(map);
            BaseController.chosenProblem.regenerate();


            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/algorithmPage.fxml")));
            BaseController.mainStage.setScene(new Scene(root));
            BaseController.mainStage.show();
        } else {
            warning.setText("Please select a problem!");
        }
    }

    /**
     * Clears warning.
     */
    public void removeWarning() {
        warning.setText("");
    }

    /**
     * Loads visualization page for datasets.
     * @throws IOException
     */
    public void visualizeData() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/visualizationPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

}
