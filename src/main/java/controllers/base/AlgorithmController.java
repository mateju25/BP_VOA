package controllers.base;

import controllers.components.MenuController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.algorithms.ArtificialBeeColonyAlgorithm;
import model.algorithms.GeneticAlgorithm;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.utils.TextFormattersFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Controller that controls the flow of page where user chooses which algorithm should be used. In views it uses algorithmPage.fxml and component views.
 */
public class AlgorithmController extends MenuController {
    @FXML
    public Label warning;
    @FXML
    public Pane infoBox;
    @FXML
    public Label infoBoxLabel;
    @FXML
    public Pane algoPane;
    @FXML
    public ChoiceBox<Algorithm> algChoiceBox;


    //GA
    public TextField numberIndividuals;
    public TextField numberGenerations;
    public TextField percentageRoulette;
    public TextField percentageTournament;
    public TextField sizeTournament;
    public TextField percentageElitism;
    public ChoiceBox<String> typeCrossover;
    public TextField percentageMutation;
    public ImageView toolRoulette;
    public ImageView toolTournament;
    public ImageView toolTournamentSize;
    public ImageView toolElitism;
    public ImageView toolCrossover;
    public ImageView toolMutation;
    public ImageView toolNumberGenerations;
    public ImageView toolNumberIndividuals;

    //ABC
    public TextField sizeBeeHive;
    public TextField numberOfIterations;
    public TextField employedBees;
    public TextField onlookerBees;
    public TextField forgetCount;
    public TextField mutationUpper;
    public TextField mutationLower;
    public ImageView toolMutationUpper;
    public ImageView toolMutationLower;
    public ImageView toolSize;
    public ImageView toolIterations;
    public ImageView toolEmployed;
    public ImageView toolOnlooker;
    public ImageView toolForget;

    //ACS
    public TextField numberOfAnts;
    public TextField pheromone;
    public TextField parameterB;
    public TextField parameterA;
    public TextField parameterQ;
    public ImageView toolAnts;
    public ImageView toolPheromone;
    public ImageView toolParamQ;
    public ImageView toolParamA;
    public ImageView toolParamB;
    private Boolean controllerLoaded = false;

    /**
     * Initialize algorithm page view. Initialize menu and all input fields if necessary. Populates algorithm choice box.
     */
    public void initialize() {
        if (controllerLoaded)
            return;
        controllerLoaded = true;
        speedChangerMenu.adjustValue(BaseController.simulationSpeed);
        initMenu();

        actualizeTextEdits();

        algChoiceBox.getSelectionModel().clearSelection();
        if (algChoiceBox != null) {
            algChoiceBox.getItems().setAll(BaseController.algorithms);
            if (BaseController.chosenAlgorithm != null) {
                var index = BaseController.algorithms.indexOf(BaseController.chosenAlgorithm);
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
            algChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                BaseController.chosenAlgorithm = algChoiceBox.getItems().get(newValue.intValue());
                Algorithm selectedAlgorithm = algChoiceBox.getItems().get(newValue.intValue());
                loadSpecificFxmlPart(selectedAlgorithm.nameOfFxmlFiles()[0]);
                BaseController.chosenAlgorithm = selectedAlgorithm;
            });
            if (BaseController.chosenAlgorithm != null) {
                algChoiceBox.getSelectionModel().select(BaseController.algorithms.indexOf(BaseController.chosenAlgorithm));
                loadSpecificFxmlPart(BaseController.chosenAlgorithm.nameOfFxmlFiles()[0]);
            } else
                algChoiceBox.getSelectionModel().select(0);
        }
    }

    /**
     * Loads specific fxml file into page a treats it as component view. Generates constraints on input fields and add tooltips.
     *
     * @param part Path to fxml component file.
     */
    private void loadSpecificFxmlPart(String part) {
        percentageRoulette = null;
        percentageTournament = null;
        percentageElitism = null;
        percentageMutation = null;
        numberIndividuals = null;
        numberGenerations = null;
        sizeTournament = null;
        sizeBeeHive = null;
        numberOfIterations = null;
        forgetCount = null;
        employedBees = null;
        mutationLower = null;
        mutationUpper = null;
        numberOfAnts = null;
        parameterB = null;
        parameterA = null;
        parameterQ = null;
        pheromone = null;
        Parent newPane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/parts/" + part)));
            loader.setController(this);
            newPane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        algoPane.getChildren().add(newPane);

        if (percentageRoulette != null) {
            typeCrossover.getItems().add("Single point crossover");
            typeCrossover.getItems().add("Double point crossover");
            typeCrossover.getSelectionModel().select(0);
            percentageRoulette.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            percentageTournament.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            percentageElitism.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            percentageMutation.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            numberIndividuals.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            numberGenerations.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            sizeTournament.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            BaseController.makeTooltip(toolRoulette, "Percentage of new individuals for generation created by roulette selection a crossover between parents");
            BaseController.makeTooltip(toolTournament, "Percentage of new individuals for generation created by tournament selection a crossover between parents");
            BaseController.makeTooltip(toolTournamentSize, "Size of tournament selection if percentage other than 0 (should not be bigger than number of individuals)");
            BaseController.makeTooltip(toolElitism, "Percentage of new individuals for generation that performed the best in the old generation");
            BaseController.makeTooltip(toolCrossover, "Type of crossover used in generating new individuals");
            BaseController.makeTooltip(toolMutation, "Power of mutation in individuals");
            BaseController.makeTooltip(toolNumberGenerations, "Number of all generation that will be run");
            BaseController.makeTooltip(toolNumberIndividuals, "Number of individuals in each generation");
        }
        if (sizeBeeHive != null) {
            sizeBeeHive.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            numberOfIterations.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            forgetCount.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            employedBees.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            mutationLower.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            mutationUpper.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            employedBees.textProperty().addListener((observable, oldValue, newValue) -> onlookerBees.setText((Math.round((1 - Double.parseDouble(newValue))*100))/100.0 + ""));
            BaseController.makeTooltip(toolSize, "Size of bee hive (number of exploited solutions to the problem)");
            BaseController.makeTooltip(toolIterations, "Number of iterations of the algorithm");
            BaseController.makeTooltip(toolEmployed, "Percentage of total bees in hive that are employed and assigned to source");
            BaseController.makeTooltip(toolOnlooker, "Percentage of total bees in hive that are onlooker and follow employed bees (together with percentage employed should be 1)");
            BaseController.makeTooltip(toolForget, "Number of iterations after which source that remained unchanged is replaced with new source");
            BaseController.makeTooltip(toolMutationUpper, "Strength of local search calculated by function where this parameter is upper bound");
            BaseController.makeTooltip(toolMutationLower, "Strength of local search calculated by function where this parameter is lower bound");
        }
        if (numberOfAnts != null) {
            numberOfAnts.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            numberOfIterations.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            parameterB.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            parameterA.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            pheromone.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            parameterQ.setTextFormatter(TextFormattersFactory.makeDoubleFormatterWithRange());
            BaseController.makeTooltip(toolAnts, "Amount of ants used in algorithm (number of exploited solutions to the problem)");
            BaseController.makeTooltip(toolIterations, "Number of iterations of the algorithm");
            BaseController.makeTooltip(toolPheromone, "Strength of pheromone evaporation level (the higher the stronger level)");
            BaseController.makeTooltip(toolParamQ, "Strength of exploitation in solution generation");
            BaseController.makeTooltip(toolParamA, "Strength of relying on pheromones in process of generating solution");
            BaseController.makeTooltip(toolParamB, "Strength of relying on heuristic function in process of generating solution");
        }

        actualizeTextEdits();
    }

    /**
     * Populates input fields if algorithm was already chosen.
     */
    private void actualizeTextEdits() {
        if (percentageRoulette != null) {
            if (BaseController.chosenAlgorithm instanceof GeneticAlgorithm) {
                if (((GeneticAlgorithm) BaseController.chosenAlgorithm).getTypeOfCrossover() != null)
                    typeCrossover.getSelectionModel().select(((GeneticAlgorithm) BaseController.chosenAlgorithm).getTypeOfCrossover());
                percentageRoulette.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageRoulette() + "");
                percentageTournament.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageTournament() + "");
                percentageElitism.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageElitism() + "");
                percentageMutation.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageMutation() + "");
                numberIndividuals.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getNumOfIndividuals() + "");
                numberGenerations.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getNumOfGenerations() + "");
                sizeTournament.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getSizeTournament() + "");
            }
        }
        if (sizeBeeHive != null) {
            if (BaseController.chosenAlgorithm instanceof ArtificialBeeColonyAlgorithm) {
                sizeBeeHive.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getSizeBeeHive() + "");
                numberOfIterations.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getNumberOfIterations() + "");
                employedBees.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getPercentageEmployed() + "");
                forgetCount.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getForgetCount() + "");
                mutationLower.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getMutationLower() + "");
                mutationUpper.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getMutationUpper() + "");
                if (((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getPercentageEmployed() != null)
                    onlookerBees.setText(Math.round((1 - ((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getPercentageEmployed())*100)/100.0 + "");
            }
        }
        if (numberOfAnts != null) {
            if (BaseController.chosenAlgorithm instanceof AntColonySystemAlgorithm) {
                numberOfAnts.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getNumberOfAnts() + "");
                numberOfIterations.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getNumberOfIterations() + "");
                parameterB.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getParameterBeta() + "");
                parameterA.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getParameterAlpha() + "");
                pheromone.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getPheromoneVapor() + "");
                parameterQ.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getParameterQ() + "");
            }
        }
    }

    /**
     * Puts property into map
     *
     * @param map   parameters map
     * @param field input field with value
     * @return if input is okay
     */
    private Boolean putIntoMap(Map<String, String> map, TextField field) {
        if (field != null && field.getText().length() > 4) {
            warning.setText("No input field should be higher than 9999!");
            return false;
        }

        if (field != null && !field.getText().equals("")) map.put(field.getId(), field.getText());
        return true;
    }


    /**
     * Check if constrains on algorithm parameters are met, initializes algorithm and loads simulation page.
     *
     * @throws IOException
     */
    public void proceed() throws IOException {
        if (BaseController.chosenProblem != null && BaseController.chosenAlgorithm != null) {
            var map = new HashMap<String, String>();
            if (!putIntoMap(map, numberIndividuals)) return;
            if (!putIntoMap(map, numberGenerations)) return;
            if (!putIntoMap(map, percentageRoulette)) return;
            if (!putIntoMap(map, percentageTournament)) return;
            if (!putIntoMap(map, sizeTournament)) return;
            if (!putIntoMap(map, percentageElitism)) return;
            if (!putIntoMap(map, percentageMutation)) return;
            if (!putIntoMap(map, sizeBeeHive)) return;
            if (!putIntoMap(map, numberOfIterations)) return;
            if (!putIntoMap(map, forgetCount)) return;
            if (!putIntoMap(map, employedBees)) return;
            if (!putIntoMap(map, numberOfAnts)) return;
            if (!putIntoMap(map, pheromone)) return;
            if (!putIntoMap(map, parameterB)) return;
            if (!putIntoMap(map, parameterA)) return;
            if (!putIntoMap(map, parameterQ)) return;
            if (!putIntoMap(map, mutationLower)) return;
            if (!putIntoMap(map, mutationUpper)) return;
            if (typeCrossover != null)
                map.put(typeCrossover.getId(), typeCrossover.getSelectionModel().getSelectedIndex() + "");

            if (BaseController.chosenAlgorithm instanceof GeneticAlgorithm && map.keySet().size() < 8) {
                warning.setText("No input field should be empty!");
                return;
            }
            if (BaseController.chosenAlgorithm instanceof ArtificialBeeColonyAlgorithm && map.keySet().size() < 6) {
                warning.setText("No input field should be empty!");
                return;
            }
            if (BaseController.chosenAlgorithm instanceof AntColonySystemAlgorithm && map.keySet().size() < 6) {
                warning.setText("No input field should be empty!");
                return;
            }

            if (sizeTournament != null && numberIndividuals != null && Integer.parseInt(sizeTournament.getText()) > Integer.parseInt(numberIndividuals.getText())) {
                warning.setText("Tournament size should not exceed number of individuals!");
                return;
            }
            if (percentageRoulette != null && percentageTournament != null && percentageElitism != null && Double.parseDouble(percentageRoulette.getText()) + Double.parseDouble(percentageTournament.getText()) + Double.parseDouble(percentageElitism.getText()) > 1.0) {
                warning.setText("Sum of percentages for roulette, tournament and elitism should not exceed 1");
                return;
            }
            if (mutationLower != null && mutationUpper != null && Double.parseDouble(mutationLower.getText()) >= Double.parseDouble(mutationUpper.getText())) {
                warning.setText("Mutation strength lower should not be higher or the same than upper bound!");
                return;
            }
            if (typeCrossover != null && typeCrossover.getSelectionModel().getSelectedItem() == null) {
                warning.setText("You need to choose type of crossover!");
                return;
            }
            if (numberIndividuals != null && Integer.parseInt(numberIndividuals.getText()) <= 1) {
                warning.setText("Number of individuals cant be less than or equal 1!");
                return;
            }
            if (sizeTournament != null && Integer.parseInt(sizeTournament.getText()) <= 1) {
                warning.setText("Size of tournament cant be less than or equal 1!");
                return;
            }
            if (sizeBeeHive != null && Integer.parseInt(sizeBeeHive.getText()) == 0) {
                warning.setText("Size of bee hive cant be 0!");
                return;
            }
            if (numberOfAnts != null && Integer.parseInt(numberOfAnts.getText()) == 0) {
                warning.setText("Number of ants cant be 0!");
                return;
            }
            if (employedBees != null && Double.parseDouble(employedBees.getText()) == 0.0) {
                warning.setText("Percentage of employed bees cant 0!");
                return;
            }

            BaseController.randomGenerator = new Random(BaseController.randomSeed);
            BaseController.chosenAlgorithm.init(map);

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/simulationPage.fxml")));
            BaseController.mainStage.setScene(new Scene(root));
            BaseController.mainStage.show();
        } else {
            warning.setText("Please select an algorithm!");
        }

    }

    /**
     * Loads main page.
     *
     * @throws IOException
     */
    public void goBack() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    /**
     * Clears warning.
     */
    public void removeWarning() {
        warning.setText("");
    }

}
