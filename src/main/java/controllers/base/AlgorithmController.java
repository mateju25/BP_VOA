package controllers.base;

import controllers.components.MenuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import model.algorithms.AntColonySystemAlgorithm;
import model.algorithms.ArtificialBeeColonyAlgorithm;
import model.algorithms.GeneticAlgorithm;
import model.problems.TargetAssignmentProblem;
import model.utils.TextFormattersFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class AlgorithmController extends MenuController {
    public Label warning;
    private Boolean controllerLoaded = false;

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

    public Label heading;
    public Pane algoPane;

    public void initialize() throws IOException {
        BaseController.randomGenerator = new Random(1);
        if (percentageRoulette != null) {
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
            employedBees.textProperty().addListener((observable, oldValue, newValue) -> onlookerBees.setText((1 - Double.parseDouble(newValue)) + ""));
            BaseController.makeTooltip(toolSize, "Size of bee hive (number of exploited solutions to the problem)");
            BaseController.makeTooltip(toolIterations, "Number of iterations of the algorithm");
            BaseController.makeTooltip(toolEmployed, "Percentage of total bees in hive that are employed and assigned to source");
            BaseController.makeTooltip(toolOnlooker, "Percentage of total bees in hive that are onlooker and follow employed bees (together with percentage employed should be 1)");
            BaseController.makeTooltip(toolForget, "Number of iterations after which source that remained unchanged is replaced with new source");
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

        if (!controllerLoaded) {
            heading.setText(BaseController.chosenAlgorithm.nameForFaces() + " parameters");
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/parts/" + BaseController.chosenAlgorithm.nameOfFxmlFiles()[0])));
            loader.setController(this);
            controllerLoaded = true;
            Parent root = loader.load();
            algoPane.getChildren().add(root);
        }
    }

    private  void actualizeTextEdits() {
        if (percentageRoulette != null) {
            if (BaseController.chosenAlgorithm instanceof GeneticAlgorithm) {
                percentageRoulette.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageRoulette()+"");
                percentageTournament.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageTournament()+"");
                percentageElitism.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageElitism()+"");
                percentageMutation.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getPercentageMutation()+"");
                numberIndividuals.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getNumOfIndividuals()+"");
                numberGenerations.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getNumOfGenerations()+"");
                sizeTournament.setText(((GeneticAlgorithm) BaseController.chosenAlgorithm).getSizeTournament()+"");
            }
        }
        if (sizeBeeHive != null) {
            if (BaseController.chosenAlgorithm instanceof ArtificialBeeColonyAlgorithm) {
                sizeBeeHive.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getSizeBeeHive()+"");
                numberOfIterations.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getNumberOfIterations()+"");
                employedBees.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getPercentageEmployed()+"");
                forgetCount.setText(((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getForgetCount()+"");
                if (((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getPercentageEmployed() != null)
                    onlookerBees.setText(1 - ((ArtificialBeeColonyAlgorithm) BaseController.chosenAlgorithm).getPercentageEmployed()+"");
            }
        }
        if (numberOfAnts != null) {
            if (BaseController.chosenAlgorithm instanceof AntColonySystemAlgorithm) {
                numberOfAnts.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getNumberOfAnts()+"");
                numberOfIterations.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getNumberOfIterations()+"");
                parameterB.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getParameterBeta()+"");
                parameterA.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getParameterAlpha()+"");
                pheromone.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getPheromoneVapor()+"");
                parameterQ.setText(((AntColonySystemAlgorithm) BaseController.chosenAlgorithm).getParameterQ()+"");
            }
        }
    }

    public void proceed() throws IOException {
        if (BaseController.chosenProblem != null && BaseController.chosenAlgorithm != null) {
            if (sizeTournament != null && numberIndividuals != null && Integer.parseInt(sizeTournament.getText()) > Integer.parseInt(numberIndividuals.getText())) {
                warning.setText("Tournament size should not exceed number of individuals!");
                return;
            }
            if (percentageRoulette != null && percentageTournament != null && percentageElitism != null && Double.parseDouble(percentageRoulette.getText()) + Double.parseDouble(percentageTournament.getText()) + Double.parseDouble(percentageElitism.getText()) > 1.0) {
                warning.setText("Sum of percentages for roulette, tournament and elitism should not exceed 1");
                return;
            }

            var map = new HashMap<String, String>();
            if (numberIndividuals != null) map.put(numberIndividuals.getId(), numberIndividuals.getText());
            if (numberGenerations != null) map.put(numberGenerations.getId(), numberGenerations.getText());
            if (percentageRoulette != null) map.put(percentageRoulette.getId(), percentageRoulette.getText());
            if (percentageTournament != null) map.put(percentageTournament.getId(), percentageTournament.getText());
            if (sizeTournament != null) map.put(sizeTournament.getId(), sizeTournament.getText());
            if (percentageElitism != null) map.put(percentageElitism.getId(), percentageElitism.getText());
            if (percentageMutation != null) map.put(percentageMutation.getId(), percentageMutation.getText());
            if (sizeBeeHive != null) map.put(sizeBeeHive.getId(), sizeBeeHive.getText());
            if (numberOfIterations != null) map.put(numberOfIterations.getId(), numberOfIterations.getText());
            if (forgetCount != null) map.put(forgetCount.getId(), forgetCount.getText());
            if (employedBees != null) map.put(employedBees.getId(), employedBees.getText());
            if (numberOfAnts != null) map.put(numberOfAnts.getId(), numberOfAnts.getText());
            if (pheromone != null) map.put(pheromone.getId(), pheromone.getText());
            if (parameterB != null) map.put(parameterB.getId(), parameterB.getText());
            if (parameterA != null) map.put(parameterA.getId(), parameterA.getText());
            if (parameterQ != null) map.put(parameterQ.getId(), parameterQ.getText());

            BaseController.chosenAlgorithm.init(map);

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/simulationPage.fxml")));
            BaseController.mainStage.setScene(new Scene(root));
            BaseController.mainStage.show();
        }

    }

    public void goBack() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }


    public void removeWarning() {
        warning.setText("");
    }

}
