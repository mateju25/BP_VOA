package controllers.base;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.algorithms.Algorithm;
import model.problems.Problem;
import model.utils.TextFormattersFactory;
import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BaseController {
    public static Algorithm chosenAlgorithm;
    public static Problem chosenProblem;

    public static List<Problem> problems;
    public static List<Algorithm> algorithms;

    public static Random randomGenerator;

    public static Stage mainStage;
    public static VisualizationController visualizationController;

    public static void makeTooltip(Node component, String message) {
        var t = new Tooltip(message);
        t.setShowDelay(Duration.seconds(0.5));
        Tooltip.install(component, t);
    }
}
