package controllers.base;

import controllers.components.DatasetPartController;
import controllers.components.MenuController;
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VisualizationController extends MenuController {


    public Label heading;
    public Pane algoPane;
    public LineChart<Integer, Double> chart;
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public Button btnAdd;
    public ListView<SimulationResults> listView;
    public Button btnExportPic;
    public Pane infoBox;
    public Label infoBoxLabel;
    public Double upperBound = 0.0;
    public Double lowerBound = 1000000.0;

    public void initialize() {
        BaseController.visualizationController = this;


        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(0);
        yAxis.setTickUnit(10);
        chart.setAnimated(false);

        if (BaseController.savedDatasets != null) {
            setUpBounds(BaseController.savedDatasets);
            for (SimulationResults simulationResults: BaseController.savedDatasets) {
                listView.getItems().add(simulationResults);

                simulationResults.setNumberOfDataset(listView.getItems().size());

                scaleEverythingAndAdd(simulationResults, true, true);

                simulationResults.setShowBest(true);
                simulationResults.setShowAverage(true);
                simulationResults.setDeleted(false);
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

    public void addDataset(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        var files = fileChooser.showOpenMultipleDialog(BaseController.mainStage.getOwner());
        if (files == null) {
            BaseController.showInfo(infoBox, infoBoxLabel, "No file selected!");
            return;
        }
        for (File file : files) {
            var results = new SimulationResults();

            try {
                results.loadFromCsv(file);
            } catch (Exception e) {
                BaseController.showInfo(infoBox, infoBoxLabel, "There was an error loading file!");
                return;
            }
            listView.getItems().add(results);
            setUpBounds(Collections.singletonList(results));
            results.setNumberOfDataset(listView.getItems().size());

            scaleEverythingAndAdd(results, true, true);

            yAxis.setTickUnit(Math.abs(yAxis.getUpperBound() - yAxis.getLowerBound()) / 15);
        }
    }

    private void setUpBounds(List<SimulationResults> results) {
        for (SimulationResults simulationResults: results) {
            if (simulationResults.getLowerBound() < lowerBound)
                lowerBound = simulationResults.getLowerBound();
            if (simulationResults.getUpperBound() > upperBound)
                upperBound = simulationResults.getUpperBound();
        }
    }

    private void scaleEverythingAndAdd(SimulationResults simulationResults, Boolean useBest, Boolean useAverage) {
        XYChart.Series<Integer, Double> seriesBest = new XYChart.Series<>();
        seriesBest.setName("Best " + simulationResults.getNumberOfDataset());
        XYChart.Series<Integer, Double> seriesAverage = new XYChart.Series<>();
        seriesAverage.setName("Average " + simulationResults.getNumberOfDataset());

        for (int i = 0; i < simulationResults.getBestFitness().size(); i++) {
            var newValue = (((100 - 0)*(simulationResults.getBestFitness().get(i) - lowerBound))/(upperBound-lowerBound)) + 0;
            seriesBest.getData().add(new XYChart.Data<>(i, newValue));
        }

        for (int i = 0; i < simulationResults.getAverageFitness().size(); i++) {
            var newValue = (((100 - 0)*(simulationResults.getAverageFitness().get(i) - lowerBound))/(upperBound-lowerBound)) + 0;
            seriesAverage.getData().add(new XYChart.Data<>(i, newValue));
        }
        if (useBest)
            chart.getData().add(seriesBest);
        if (useAverage)
            chart.getData().add(seriesAverage);
    }

    public void somethingChanged() {
        chart.getData().clear();
        var list = new ArrayList<SimulationResults>();
        setUpBounds(listView.getItems());
        for (SimulationResults simRes : listView.getItems()) {
            if (simRes.getDeleted()) {
                continue;
            }
            if (simRes.getShowAverage()) {
                scaleEverythingAndAdd(simRes, false, true);
            }
            if (simRes.getShowBest()) {
                scaleEverythingAndAdd(simRes, true, false);
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
