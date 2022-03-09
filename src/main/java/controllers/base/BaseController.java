package controllers.base;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.algorithms.Algorithm;
import model.algorithms.AntColonySystemAlgorithm;
import model.algorithms.ArtificialBeeColonyAlgorithm;
import model.algorithms.GeneticAlgorithm;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.problems.TargetAssignmentProblem;
import model.problems.VehicleRoutingProblem;
import model.utils.SimulationResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controllers that share dat between other controllers. Also provides some basic tools for frontend.
 */
public class BaseController {
    public static Algorithm chosenAlgorithm;
    public static Problem chosenProblem;
    public static Boolean isProblemGenerated = false;

    public static List<Problem> problems;
    public static List<Algorithm> algorithms;

    public static Integer simulationSpeed = 0;
    public static Integer oldSimSpeed = null;
    public static Integer randomSeed = 1;
    public static Random randomGenerator = new Random(BaseController.randomSeed);

    public static Stage mainStage;
    public static VisualizationController visualizationController;
    public static Boolean simulationChart;

    public static ArrayList<SimulationResults> savedDatasets;
    public static boolean isFirstLoad = true;

    /**
     * Generates all problems and algorithms.
     */
    public static void init() {
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
    }

    /**
     * Generates tooltip.
     * @param component Node component that tooltip is assigned to.
     * @param message Text available in tooltip.
     */
    public static void makeTooltip(Node component, String message) {
        var t = new Tooltip(message);
        t.setShowDelay(Duration.seconds(0.5));
        Tooltip.install(component, t);
    }

    /**
     * Shows info box.
     * @param pane Pane that contains info label.
     * @param label Label which text will be populated.
     * @param message Text that will be used.
     */
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
