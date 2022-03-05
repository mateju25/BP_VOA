package controllers.base;

import controllers.components.MenuController;
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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Controllers that controls flow of simulation of algorithm.
 */
public class SimulationController extends MenuController {
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
    @FXML
    public Slider speedChanger;
    @FXML
    public Canvas canvas;
    @FXML
    public NumberAxis yAxis;
    @FXML
    public Button btnSave;
    @FXML
    public Button btnRestart;
    @FXML
    public Button btnSaveD;
    @FXML
    public NumberAxis xAxis;
    @FXML
    public Pane infoBox;
    @FXML
    public Label infoBoxLabel;
    public Button btnRandomize;
    public Label lblBest;
    public Label lblAverage;
    public Pane moreSimsPane;
    public Label lblSimulationNumber;
    public Button btnMoreSims;
    public Button btnSwitch;
    public Button btnBack;
    public Label lblTime;

    private ExecutorService executorService;
    private AnimationTimer animationTimer;
    private Boolean simulationRunning = true;
    private Boolean moreSimulationRunning = true;
    private Boolean simulationRestart = false;
    private Integer simulationSpeed = BaseController.simulationSpeed;
    private long elapsedTime = 0;
    private SimulationResults results;

    /**
     * Initialize simulation page view. Prepares chart and canvas component. Creates animator for chart and main thread for algorithm run.
     */
    public void initialize() {
        speedChangerMenu.adjustValue(BaseController.simulationSpeed);
        speedChanger.adjustValue(simulationSpeed);
        initMenu();

        simulationRestart = false;
        moreSimsPane.setVisible(false);
        btnMoreSims.setDisable(true);
        btnSave.setDisable(true);
        btnSaveD.setDisable(true);

        prepareControlButtons();

        results = new SimulationResults(BaseController.chosenAlgorithm.nameForFaces() + " solves " + BaseController.chosenProblem.nameForFaces());
        heading.setText(BaseController.chosenAlgorithm.nameForFaces() + " solves " + BaseController.chosenProblem.nameForFaces());

        if (BaseController.simulationChart == null)
            BaseController.simulationChart = true;
        canvas.setVisible(!BaseController.simulationChart);
        chart.setVisible(BaseController.simulationChart);

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
                    long delta = System.currentTimeMillis();
                    AlgorithmResults res = BaseController.chosenAlgorithm.nextGeneration();
                    elapsedTime += System.currentTimeMillis() - delta;

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
                setBoundaries(data);
                BaseController.chosenProblem.visualize(canvas, data);

                if (data.getActualGeneration().equals(data.getMaxGeneration())) {
                    btnSave.setDisable(false);
                    btnSaveD.setDisable(false);
                    btnRandomize.setDisable(false);
                    btnMoreSims.setDisable(false);
                    lblTime.setText(elapsedTime/1000.0+" s");
                }
                lblAverage.setText(String.format("%,.4f", data.getAverageFitnessInGen()));
                lblBest.setText(String.format("%,.4f", data.getBestFitness()));
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

    /**
     * Changes boundaries that chart shows.
     * @param data Algorithm results from which best and average fitness is used.
     */
    private void setBoundaries(AlgorithmResults data) {
        var highBound = yAxis.getUpperBound();
        if (highBound < data.getAverageFitnessInGen() + 0.5) {
            yAxis.setUpperBound(data.getAverageFitnessInGen() + 0.5);
        }
        var lowBound = yAxis.getLowerBound();
        if (lowBound > data.getBestFitness() - 0.5) {
            yAxis.setLowerBound(data.getBestFitness() - 0.5);
        }
        yAxis.setTickUnit(Math.abs(highBound - lowBound) / 15);
    }

    /**
     * Prepares control buttons for simulation page.
     */
    private void prepareControlButtons() {
        btnRestart.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/restart.png")))));
        btnPause.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/pause.png")))));
        btnContinue.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/play.png")))));
        btnRandomize.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/shuffle.png")))));
        btnContinue.setDisable(true);
        btnRandomize.setDisable(true);

        BaseController.makeTooltip(btnRestart, "Restart simulation");
        BaseController.makeTooltip(btnPause, "Pause simulation");
        BaseController.makeTooltip(btnContinue, "Continue simulation");
        BaseController.makeTooltip(btnRandomize, "Generate new problem");
    }

    /**
     * Stops simulation and go back to algorithm page.
     * @throws IOException
     */
    public void goBack() throws IOException {
        simulationRestart = true;
        animationTimer.stop();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/algorithmPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    /**
     * Unpauses simulation.
     */
    public void continueSim() {
        simulationRunning = true;
        btnContinue.setDisable(true);
        btnPause.setDisable(false);
    }

    /**
     * Pauses simulation.
     */
    public void pauseSim() {
        simulationRunning = false;
        btnContinue.setDisable(false);
        btnPause.setDisable(true);
    }

    /**
     * Restarts simulation.
     * @throws IOException
     */
    public void restartSim() throws IOException {
        int oldSimSpeed = simulationSpeed;
        executorService.shutdownNow();
        elapsedTime = 0;
        simulationRestart = true;
        animationTimer.stop();
        chart.getData().clear();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/simulationPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
        speedChanger.adjustValue(oldSimSpeed);
    }

    /**
     * Switches between chart view and canvas view.
     */
    public void switchVisualization() {
        BaseController.simulationChart = !BaseController.simulationChart;
        canvas.setVisible(!BaseController.simulationChart);
        chart.setVisible(BaseController.simulationChart);
    }

    /**
     * Saves a simulation run that will be available in visualization page.
     */
    public void addToVisualization() {
        btnSaveD.setDisable(true);
        if (BaseController.savedDatasets == null)
            BaseController.savedDatasets = new ArrayList<>();

        results.setUpperBound(yAxis.getUpperBound() - 0.5);
        results.setLowerBound(yAxis.getLowerBound() + 0.5);
        BaseController.savedDatasets.add(results);
        BaseController.showInfo(infoBox, infoBoxLabel, "Simulation results added to visualization page!");
    }

    /**
     * Regenerates problem.
     */
    public void randomizeProblem() {
        BaseController.chosenProblem.regenerate();
        BaseController.showInfo(infoBox, infoBoxLabel, "Problem was regenerated!");
    }

    /**
     * Run 100 simulations.
     */
    public void runSims() {
        moreSimsPane.setVisible(true);

        btnMoreSims.setDisable(true);
        btnSwitch.setDisable(true);
        btnSaveD.setDisable(true);
        btnSave.setDisable(true);
        btnBack.setDisable(true);
        btnContinue.setDisable(true);
        btnPause.setDisable(true);
        btnRandomize.setDisable(true);
        btnRestart.setDisable(true);
        speedChanger.setDisable(true);

        moreSimulationRunning = true;
        var mapBest = new HashMap<Integer, List<Double>>();
        var mapAverage = new HashMap<Integer, List<Double>>();

        ConcurrentLinkedQueue<Integer> simNumber = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<XYChart.Series> series = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<AlgorithmResults> bestRes = new ConcurrentLinkedQueue<>();


        executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        class AddToQueue implements Runnable {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    if (!moreSimulationRunning)
                        break;
                    simNumber.add(i);

                    BaseController.chosenAlgorithm.setProblem(BaseController.chosenProblem);
                    BaseController.chosenAlgorithm.resetAlgorithm();
                    BaseController.chosenAlgorithm.initFirstGeneration();



                    AlgorithmResults res = BaseController.chosenAlgorithm.nextGeneration();
                    while (res != null) {
                        if (!moreSimulationRunning)
                            break;

                        mapBest.computeIfAbsent(res.getActualGeneration(), k -> new ArrayList<>());
                        mapAverage.computeIfAbsent(res.getActualGeneration(), k -> new ArrayList<>());

                        mapBest.get(res.getActualGeneration()).add(res.getBestFitness());
                        mapAverage.get(res.getActualGeneration()).add(res.getAverageFitnessInGen());

                        if (bestRes.size() == 0)
                            bestRes.add(res);
                        else {
                            if (bestRes.peek().getBestFitness() > res.getBestFitness()) {
                                bestRes.clear();
                                bestRes.add(res);
                            }
                        }
                        long delta = System.currentTimeMillis();
                        res = BaseController.chosenAlgorithm.nextGeneration();
                        elapsedTime += System.currentTimeMillis() - delta;

                        if (res != null)
                            setBoundaries(res);
                    }
                }
                moreSimsPane.setVisible(false);

                XYChart.Series<Integer, Double> seriesBest = new XYChart.Series<>();
                seriesBest.setName("Best");
                XYChart.Series<Integer, Double> seriesAverage = new XYChart.Series<>();
                seriesAverage.setName("Average");

                for (Integer key : mapBest.keySet()) {
                    seriesBest.getData().add(new XYChart.Data<>(key, mapBest.get(key).stream().mapToDouble(e -> e).average().getAsDouble()));
                }
                for (Integer key : mapAverage.keySet()) {
                    seriesAverage.getData().add(new XYChart.Data<>(key, mapAverage.get(key).stream().mapToDouble(e -> e).average().getAsDouble()));
                }

                series.add(seriesBest);
                series.add(seriesAverage);
                simNumber.add(100);

            }
        }
        executorService.execute(new AddToQueue());

        animationTimer = new AnimationTimer() {
            @SneakyThrows
            @Override
            public void handle(long now) {
                if (simNumber.isEmpty()) return;
                var data = simNumber.remove();
                lblSimulationNumber.setText(data + "/100");

                if (data == 100) {
                    chart.getData().clear();
                    chart.getData().add(series.remove());
                    chart.getData().add(series.remove());

                    var bestbest = bestRes.remove();
                    BaseController.chosenProblem.visualize(canvas, bestbest);

                    lblAverage.setText(String.format("%,.4f", bestbest.getAverageFitnessInGen()));
                    lblBest.setText(String.format("%,.4f", bestbest.getBestFitness()));
                    lblTime.setText(elapsedTime/1000.0+" s");

                    btnMoreSims.setDisable(false);
                    btnSwitch.setDisable(false);
                    btnSaveD.setDisable(false);
                    btnSave.setDisable(false);
                    btnBack.setDisable(false);
                    btnPause.setDisable(false);
                    btnRandomize.setDisable(false);
                    btnRestart.setDisable(false);
                    speedChanger.setDisable(false);
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Disables all buttons during 100 simulation run.
     */
    public void cancelSimulations() {
        moreSimulationRunning = false;
        moreSimsPane.setVisible(false);
        btnMoreSims.setDisable(false);
        btnSwitch.setDisable(false);
        btnSaveD.setDisable(false);
        btnSave.setDisable(false);
        btnBack.setDisable(false);
        btnPause.setDisable(false);
        btnRandomize.setDisable(false);
        btnRestart.setDisable(false);
        speedChanger.setDisable(false);
    }
}
