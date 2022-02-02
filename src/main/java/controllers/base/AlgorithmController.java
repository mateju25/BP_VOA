package controllers.base;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import model.algorithms.GeneticAlgorithm;
import model.utils.TextFormattersFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class AlgorithmController {
    private Boolean controllerLoaded = false;

    public TextField numberIndividuals;
    public TextField numberGenerations;
    public TextField percentageRoulette;
    public TextField percentageTournament;
    public TextField sizeTournament;
    public TextField percentageElitism;
    public ChoiceBox typeCrossover;
    public TextField percentageMutation;
    public Label heading;
    public Pane algoPane;

    public void initialize() throws IOException {
        BaseController.rndm = new Random(1);
        if (percentageRoulette != null) {
            percentageRoulette.setTextFormatter(TextFormattersFactory.makeDoubleFormatter());
            percentageTournament.setTextFormatter(TextFormattersFactory.makeDoubleFormatter());
            percentageElitism.setTextFormatter(TextFormattersFactory.makeDoubleFormatter());
            percentageMutation.setTextFormatter(TextFormattersFactory.makeDoubleFormatter());
            numberIndividuals.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            numberGenerations.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            sizeTournament.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
        }

        if (!controllerLoaded) {
            heading.setText(BaseController.chosedAlgorithm.nameForFaces() + " parameters");
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/parts/" + BaseController.chosedAlgorithm.nameOfFxmlFiles()[0])));
            loader.setController(this);
            controllerLoaded = true;
            Parent root = loader.load();
            algoPane.getChildren().add(root);
        }
    }

    public void proceed(ActionEvent actionEvent) throws IOException {
        ((GeneticAlgorithm) BaseController.chosedAlgorithm).setAlgorithm(Integer.valueOf(numberIndividuals.getText()),
                Integer.valueOf(numberGenerations.getText()), Double.valueOf(percentageRoulette.getText()),
                Double.valueOf(percentageTournament.getText()), Integer.valueOf(sizeTournament.getText()),
                Double.valueOf(percentageElitism.getText()), Double.valueOf(percentageMutation.getText()));
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/simulationPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }
}
