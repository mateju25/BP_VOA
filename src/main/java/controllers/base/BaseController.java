package controllers.base;

import javafx.stage.Stage;
import model.algorithms.Algorithm;
import model.problems.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BaseController {
    public static Algorithm chosedAlgorithm;
    public static Problem chosedProblem;

    public static List<Problem> problems;
    public static List<Algorithm> algorithms;

    public static Random rndm;

    public static Stage mainStage;
}
