package controllers.base;

import controllers.listCells.DatasetPartController;
import javafx.embed.swing.SwingFXUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import model.utils.SimulationResults;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VisualizationController {


    public Label heading;
    public Pane algoPane;
    public LineChart<Integer, Double> chart;
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public Button btnAdd;
    public ListView<SimulationResults> listView;
    public Button btnExportPic;

    public void initialize() {
        BaseController.visualizationController = this;


        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(10000);
        yAxis.setUpperBound(1);
        chart.setAnimated(false);

        if (BaseController.savedDatasets != null) {
            for (SimulationResults simulationResults: BaseController.savedDatasets) {
                listView.getItems().add(simulationResults);

                simulationResults.setNumberOfDataset(listView.getItems().size());

                addDataToChart(simulationResults.getBestFitness(), "Best " + simulationResults.getNumberOfDataset());
                addDataToChart(simulationResults.getAverageFitness(), "Average " + simulationResults.getNumberOfDataset());


                simulationResults.setShowBest(true);
                simulationResults.setShowAverage(true);
                simulationResults.setDeleted(false);

                yAxis.setTickUnit(Math.abs(yAxis.getUpperBound() - yAxis.getLowerBound()) / 15);
            }
            BaseController.savedDatasets = null;
        }


        listView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Platform.runLater(() -> listView.getSelectionModel().select(-1));
            }
        });
        listView.setCellFactory(param -> new DatasetPartController());
    }

    public void goBack() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void addDataset() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        var files = fileChooser.showOpenMultipleDialog(BaseController.mainStage.getOwner());
        if (files == null)
            return;

        for (File file : files) {
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

    public void somethingChanged() {
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

    public void exportPicture() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.png"));

        var formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
        fileChooser.setInitialFileName(LocalDateTime.now().format(formatter));
        var file = fileChooser.showSaveDialog(BaseController.mainStage);
        if (file != null) {
            WritableImage image = chart.snapshot(new SnapshotParameters(), null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
        }
    }
}
