package controllers.base;

import controllers.components.MenuController;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import model.utils.AlgorithmResults;
import model.utils.SimulationResults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationController extends MenuController {
    @FXML public Label heading;
    @FXML public Pane algoPane;
    @FXML public LineChart<Integer, Double> chart;
    @FXML public Button btnContinue;
    @FXML public Button btnPause;
    @FXML public Slider speedChanger;
    @FXML public Canvas canvas;
    @FXML public NumberAxis yAxis;
    @FXML public Button btnSave;
    @FXML public Button btnRestart;
    @FXML public Button btnSaveD;
    @FXML public NumberAxis xAxis;
    @FXML public Pane infoBox;
    @FXML public Label infoBoxLabel;

    private ExecutorService executorService;
    private AnimationTimer animationTimer;
    private Boolean simulationRunning = true;
    private Boolean simulationRestart = false;
    private Boolean simulationChart = true;
    private Integer simulationSpeed = BaseController.simulationSpeed;
    private SimulationResults results;


    public void initialize() {
        speedChangerMenu.adjustValue(BaseController.simulationSpeed);
        speedChanger.adjustValue(BaseController.simulationSpeed);
        initMenu();

        simulationRestart = false;
        btnSave.setDisable(true);
        btnSaveD.setDisable(true);

        prepareControlButtons();

        results = new SimulationResults(BaseController.chosenAlgorithm.nameForFaces() + " solves " + BaseController.chosenProblem.nameForFaces());
        heading.setText(BaseController.chosenAlgorithm.nameForFaces() + " solves " + BaseController.chosenProblem.nameForFaces());

        canvas.setVisible(!simulationChart);

        XYChart.Series<Integer, Double> seriesBest = new XYChart.Series<>();
        seriesBest.setName("Best");
        XYChart.Series<Integer, Double> seriesAverage = new XYChart.Series<>();
        seriesAverage.setName("Average");

        ConcurrentLinkedQueue<AlgorithmResults> dataQ = new ConcurrentLinkedQueue<>();

        yAxis.setAutoRanging(false);
        chart.setAnimated(false);
        chart.getData().add(seriesBest);
        chart.getData().add(seriesAverage);

        BaseController.randomGenerator = new Random(BaseController.randomSeed);
        BaseController.chosenAlgorithm.setProblem(BaseController.chosenProblem);
        BaseController.chosenAlgorithm.resetAlgorithm();
        BaseController.chosenAlgorithm.initFirstGeneration();

        executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        class AddToQueue implements Runnable {
            public void run() {
                try {
                    AlgorithmResults res = BaseController.chosenAlgorithm.nextGeneration();

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
                } catch (InterruptedException ignored) {
                }
            }
        }
        executorService.execute(new AddToQueue());

        animationTimer = new AnimationTimer() {
            @SneakyThrows
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
                BaseController.chosenAlgorithm.getProblem().visualize(canvas, data);

                if (data.getActualGeneration().equals(data.getMaxGeneration())) {
                    btnSave.setDisable(false);
                    btnSaveD.setDisable(false);
                }
            }
        };

        animationTimer.start();

        speedChanger.valueProperty().addListener(new ChangeListener<>() {

            @Override
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {

                simulationSpeed = newValue.intValue();
            }

        });

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        btnSave.setOnAction(event -> {
            var formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
            fileChooser.setInitialFileName(BaseController.chosenAlgorithm.nameForFaces().chars().filter(Character::isUpperCase)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString() + "_" + LocalDateTime.now().format(formatter));
            try {
                var file = fileChooser.showSaveDialog(BaseController.mainStage);
                if (file != null) {
//                    results.writeToCsv(file, yAxis.getUpperBound(), yAxis.getLowerBound());
                    results.writeToJson(file, yAxis.getUpperBound(), yAxis.getLowerBound(), BaseController.chosenProblem, BaseController.chosenAlgorithm);
                    BaseController.showInfo(infoBox, infoBoxLabel, "Simulation saved!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void prepareControlButtons() {
        btnRestart.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/restart.png")))));
        btnPause.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/pause.png")))));
        btnContinue.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/play.png")))));
        btnContinue.setDisable(true);

        BaseController.makeTooltip(btnRestart, "Restart");
        BaseController.makeTooltip(btnPause, "Pause");
        BaseController.makeTooltip(btnContinue, "Continue");
    }

    public void goBack() throws IOException {
        simulationRestart = true;
        animationTimer.stop();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/algorithmPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void continueSim() {
        simulationRunning = true;
        btnContinue.setDisable(true);
        btnPause.setDisable(false);
    }

    public void pauseSim() {
        simulationRunning = false;
        btnContinue.setDisable(false);
        btnPause.setDisable(true);
    }

    public void restartSim() throws IOException {
        executorService.shutdownNow();
        simulationRestart = true;
        animationTimer.stop();
        chart.getData().clear();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/simulationPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void switchVisualization() {
        simulationChart = !simulationChart;
        canvas.setVisible(!simulationChart);
        chart.setVisible(simulationChart);
    }

    public void addToVisualization() {
        btnSaveD.setDisable(true);
        if (BaseController.savedDatasets == null)
            BaseController.savedDatasets = new ArrayList<>();

        results.setUpperBound(yAxis.getUpperBound() - 0.5);
        results.setLowerBound(yAxis.getLowerBound() + 0.5);
        BaseController.savedDatasets.add(results);
        BaseController.showInfo(infoBox, infoBoxLabel, "Simulation results added to visualization page!");
    }
}
