package controllers.base;

import controllers.listCells.DatasetPartController;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import model.utils.AlgorithmResults;
import model.utils.SimulationResults;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class VisualizationController {


    public Label heading;
    public Pane algoPane;
    public LineChart<Integer, Double> chart;
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public Button btnAdd;
    public ListView<SimulationResults> listView;

    public void initialize() {
        BaseController.visualizationController = this;

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(10000);
        yAxis.setUpperBound(1);
        chart.setAnimated(false);

        listView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        listView.getSelectionModel().select(-1);

                    }
                });

            }
        });
        listView.setCellFactory(param -> new DatasetPartController());
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void addDataset(ActionEvent actionEvent) throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        var files = fileChooser.showOpenMultipleDialog(BaseController.mainStage.getOwner());
        if (files == null)
            return;

        for (File file: files) {
            var results = new SimulationResults();
            listView.getItems().add(results);

            results.loadFromCsv(file);
            results.setNumberOfDataset(listView.getItems().size());

            addDataToChart(results.getBestFitness(), "Best " + results.getNumberOfDataset());
            addDataToChart(results.getAverageFitness(), "Average " + results.getNumberOfDataset());


            yAxis.setTickUnit(Math.abs(yAxis.getUpperBound() - yAxis.getLowerBound()) / 15);
        }
    }

    private void addDataToChart(List<Double> data, String message) {
        XYChart.Series<Integer, Double> series = new XYChart.Series<>();
        series.setName(message);
        for (int i = 0; i < data.size(); i++) {
            var highBound = yAxis.getUpperBound();
            if (highBound < data.get(i) + 0.5) {
                yAxis.setUpperBound(data.get(i) + 0.5);
            }
            var lowBound = yAxis.getLowerBound();
            if (lowBound > data.get(i) - 0.5) {
                yAxis.setLowerBound(data.get(i) - 0.5);
            }
            series.getData().add(new XYChart.Data<>(i, data.get(i)));
        }
        chart.getData().add(series);

    }

    public void smthChanged() {
        chart.getData().clear();
        var list = new ArrayList<SimulationResults>();
        for (SimulationResults simRes : listView.getItems()) {
            if (simRes.getDeleted()) {
                continue;
            }
            if (simRes.getShowAverage()) {
                addDataToChart(simRes.getAverageFitness(), "Average " + simRes.getNumberOfDataset());
            }
            if (simRes.getShowBest()) {
                addDataToChart(simRes.getBestFitness(), "Best " + simRes.getNumberOfDataset());
            }
            list.add(simRes);
        }
        listView.getItems().setAll(list);
    }
}
