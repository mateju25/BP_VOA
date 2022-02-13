package controllers.base;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import model.algorithms.GeneticAlgorithm;
import model.utils.AlgorithmResults;
import model.utils.SimulationResults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
    public NumberAxis yAxis;
    public Button btnSave;

    private ExecutorService executorService;
    private AnimationTimer animationTimer;
    private Boolean simulationRunning = true;
    private Boolean simulationRestart = false;
    private Boolean simulationChart = true;
    private Integer simulationSpeed = 200;
    private SimulationResults results;


    public void initialize() {
        BaseController.rndm = new Random(1);
        results = new SimulationResults(BaseController.chosedAlgorithm.nameForFaces() + " solves " + BaseController.chosedProblem.nameForFaces());
        btnContinue.setDisable(true);
        canvas.setVisible(!simulationChart);

        lblSpeed.setText(simulationSpeed + " ms");
        heading.setText(BaseController.chosedAlgorithm.nameForFaces() + " solves " + BaseController.chosedProblem.nameForFaces());

        XYChart.Series<Integer, Double> seriesBest = new XYChart.Series<>();
        seriesBest.setName("Best");
        XYChart.Series<Integer, Double> seriesAverage = new XYChart.Series<>();
        seriesAverage.setName("Average");

        ConcurrentLinkedQueue<AlgorithmResults> dataQ = new ConcurrentLinkedQueue<>();

        yAxis.setAutoRanging(false);
        chart.setAnimated(false);
        chart.getData().add(seriesBest);
        chart.getData().add(seriesAverage);

        BaseController.chosedAlgorithm.setProblem(BaseController.chosedProblem);
        BaseController.chosedAlgorithm.initFirstGeneration();

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
                    AlgorithmResults res = BaseController.chosedAlgorithm.nextGeneration();

                    if (res != null) {
                        dataQ.add(res);
                        results.addData(res.getAverageFitnessInGen(), res.getBestFitness());

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
                var highBound = yAxis.getUpperBound();
                if (highBound < data.getAverageFitnessInGen() + 0.5) {
                    yAxis.setUpperBound(data.getAverageFitnessInGen() + 0.5);
                }
                var lowBound = yAxis.getLowerBound();
                if (lowBound > data.getBestFitness() - 0.5) {
                    yAxis.setLowerBound(data.getBestFitness() - 0.5);
                }
                yAxis.setTickUnit(Math.abs(highBound - lowBound) / 15);
                BaseController.chosedAlgorithm.getProblem().visualize(canvas, data);
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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.csv"));
        btnSave.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                var formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
                fileChooser.setInitialFileName(BaseController.chosedAlgorithm.nameForFaces().chars().filter(Character::isUpperCase)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString() + "_" + LocalDateTime.now().format(formatter));
                try {
                    results.writeToCsv(fileChooser.showSaveDialog(BaseController.mainStage));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
