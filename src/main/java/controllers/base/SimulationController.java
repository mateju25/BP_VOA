package controllers.base;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import model.algorithms.GeneticAlgorithm;
import model.problems.KnapsackProblem;
import model.problems.Problem;
import model.utils.AlgorithmResults;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BinaryOperator;

public class SimulationController {
    @FXML
    public Label heading;
    @FXML
    public Pane algoPane;
    @FXML
    public LineChart<Integer, Double> chart;
    @FXML
    public Button btnContinue;
    @FXML
    public Button btnPause;
    public Slider speedChanger;
    public Label lblSpeed;
    public Canvas canvas;

    private ExecutorService executorService;
    private AnimationTimer animationTimer;
    private Boolean simulationRunning = true;
    private Boolean simulationRestart = false;
    private Boolean simulationChart = true;
    private Integer simulationSpeed = 200;


    public void initialize() {
        BaseController.rndm = new Random(1);
        btnContinue.setDisable(true);
        canvas.setVisible(!simulationChart);

        lblSpeed.setText(simulationSpeed + " ms");
        heading.setText(BaseController.chosedAlgorithm.nameForFaces() + " solves " + BaseController.chosedProblem.nameForFaces());

        XYChart.Series<Integer, Double> seriesBest = new XYChart.Series<>();
        seriesBest.setName("Best");
        XYChart.Series<Integer, Double> seriesAverage = new XYChart.Series<>();
        seriesAverage.setName("Average");

        ConcurrentLinkedQueue<AlgorithmResults> dataQ = new ConcurrentLinkedQueue<>();

        chart.getYAxis().setAutoRanging(false);
        chart.setAnimated(false);
        chart.getData().add(seriesBest);
        chart.getData().add(seriesAverage);

        GeneticAlgorithm ga = (GeneticAlgorithm) BaseController.chosedAlgorithm;
        ga.setProblem(BaseController.chosedProblem);
//        kp.populateProblem(100, 4, 500);
//        ga.setAlgorithm(100, 100, 0.2, 0.1, 10, 0.5, 0.5);
        ga.initFirstGeneration();

        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        class AddToQueue implements Runnable {
            public void run() {
                try {
                    AlgorithmResults res = ga.nextGeneration();

                    if (res != null) {
                        dataQ.add(res);

                        Thread.sleep(simulationSpeed);
                        while (!simulationRunning) {
                            Thread.sleep(50);
                        }
                        if (!simulationRestart)
                            executorService.execute(this);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        simulationRestart = false;
        executorService.execute(new AddToQueue());

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (dataQ.isEmpty()) return;
                var data = dataQ.remove();
                seriesBest.getData().add(new XYChart.Data<>(data.getActualGeneration() - 1, data.getBestFitness()));
                seriesAverage.getData().add(new XYChart.Data<>(data.getActualGeneration() - 1, data.getAverageFitnessInGen()));
                var highBound = ((ValueAxis<Double>) chart.getYAxis()).getUpperBound();
                if (highBound < Math.round(data.getAverageFitnessInGen()) + 0.5) {
                    ((ValueAxis<Double>) chart.getYAxis()).setUpperBound(Math.round(data.getAverageFitnessInGen()) + 0.5);
                }

                ga.getProblem().visualize(canvas, data);
            }
        };

        animationTimer.start();

        speedChanger.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {

                simulationSpeed = newValue.intValue();
                lblSpeed.setText(simulationSpeed + " ms");

            }

        });

    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        simulationRestart = true;
        animationTimer.stop();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/algorithmPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void continueSim(ActionEvent actionEvent) {
        simulationRunning = true;
        btnContinue.setDisable(true);
        btnPause.setDisable(false);
    }

    public void pauseSim(ActionEvent actionEvent) {
        simulationRunning = false;
        btnContinue.setDisable(false);
        btnPause.setDisable(true);
    }

    public void restartSim(ActionEvent actionEvent) {
        simulationRestart = true;
        animationTimer.stop();
        chart.getData().clear();
        BaseController.chosedAlgorithm.resetAlgorithm();
        initialize();
    }

    public void switchVisualization(ActionEvent actionEvent) {
        simulationChart = !simulationChart;
        canvas.setVisible(!simulationChart);
        chart.setVisible(simulationChart);
    }
}
