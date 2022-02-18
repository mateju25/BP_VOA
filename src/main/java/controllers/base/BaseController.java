package controllers.base;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.algorithms.Algorithm;
import model.problems.Problem;
import model.utils.SimulationResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BaseController {
    public static Algorithm chosenAlgorithm;
    public static Problem chosenProblem;

    public static List<Problem> problems;
    public static List<Algorithm> algorithms;

    public static Random randomGenerator;

    public static Stage mainStage;
    public static VisualizationController visualizationController;

    public static Boolean isFirstLoad = true;

    public static ArrayList<SimulationResults> savedDatasets;

    public static void makeTooltip(Node component, String message) {
        var t = new Tooltip(message);
        t.setShowDelay(Duration.seconds(0.5));
        Tooltip.install(component, t);
    }

    public static void showInfo(Pane pane, Label label, String message) {
        pane.setOpacity(1.0);
        label.setText(message);
        FadeTransition ft = new FadeTransition(Duration.millis(2000), pane);
        ft.setDelay(Duration.millis(1500));
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.play();
    }
}
