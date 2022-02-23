package controllers.base;

import controllers.components.CrossHairLineChart;
import controllers.components.DatasetPartController;
import controllers.components.MenuController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
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
    @FXML
    public Label heading;
    @FXML
    public Pane algoPane;
    public CrossHairLineChart<Integer, Double> chart;
    public NumberAxis xAxis = new NumberAxis();
    ;
    public NumberAxis yAxis = new NumberAxis();
    ;
    @FXML
    public Button btnAdd;
    @FXML
    public ListView<SimulationResults> listView;
    @FXML
    public Button btnExportPic;
    @FXML
    public Pane infoBox;
    @FXML
    public Label infoBoxLabel;
    @FXML
    public AnchorPane anchorPane;

    public Double upperBound;
    public Double lowerBound;

    public void initialize() {
        BaseController.visualizationController = this;
        listView.getItems().clear();

        ToggleButton toggleButton = createToggleButton();
        createChart(toggleButton);

        upperBound = 0.0;
        lowerBound = 1000000.0;

        setupAxises();

        if (BaseController.savedDatasets != null) {
            setUpBounds(BaseController.savedDatasets);
            for (SimulationResults simulationResults : BaseController.savedDatasets) {
                listView.getItems().add(simulationResults);

                simulationResults.setNumberOfDataset(listView.getItems().size());

                scaleEverythingAndAdd(simulationResults, true, true);

                simulationResults.setShowBest(true);
                simulationResults.setShowAverage(true);
                simulationResults.setDeleted(false);
            }
        }

        addTooltipForNodesInChart();


        listView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Platform.runLater(() -> listView.getSelectionModel().select(-1));
            }
        });
        listView.setCellFactory(param -> new DatasetPartController());
    }

    private void addTooltipForNodesInChart() {
        for (XYChart.Series<Integer, Double> s : (ObservableList<XYChart.Series<Integer, Double>>) chart.getData()) {
            for (XYChart.Data<Integer, Double> d : s.getData()) {
                var tooltip = new Tooltip(
                        "Generation: " + d.getXValue().toString() + "\n" +
                                "Fitness : " + d.getYValue());
                tooltip.setShowDelay(Duration.millis(10));
                Tooltip.install(d.getNode(), tooltip);

                //Adding class on hover
                d.getNode().setOnMouseEntered(event ->
                        d.getNode().getStyleClass().add("onHover"));

                //Removing class on exit
                d.getNode().setOnMouseExited(event ->
                        d.getNode().getStyleClass().remove("onHover"));
            }
        }
    }

    private void setupAxises() {
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(0);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickCount(8);
        yAxis.setSide(Side.LEFT);
        yAxis.setLabel("Fitness");
        xAxis.setSide(Side.BOTTOM);
        xAxis.setLabel("Generations");
    }

    private void createChart(ToggleButton toggleButton) {
        chart = new CrossHairLineChart<Integer, Double>((Axis) xAxis, (Axis) yAxis, toggleButton);
        chart.setLayoutY(11);
        chart.setPrefHeight(527.0);
        chart.setPrefWidth(1031.0);
        chart.setAnimated(false);
        algoPane.getChildren().add(chart);
    }

    private ToggleButton createToggleButton() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setLayoutX(359.0);
        toggleButton.setLayoutY(37.0);
        toggleButton.setPrefHeight(32);
        toggleButton.setPrefWidth(32);
        toggleButton.getStyleClass().add("button-black");
        toggleButton.setFocusTraversable(false);
        toggleButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ruler.png")))));
        anchorPane.getChildren().add(toggleButton);
        return toggleButton;
    }

    public void goBack() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    public void addDataset() {
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
            if (BaseController.savedDatasets == null)
                BaseController.savedDatasets = new ArrayList<>();
            BaseController.savedDatasets.add(results);
            setUpBounds(Collections.singletonList(results));
            results.setNumberOfDataset(listView.getItems().size());

            scaleEverythingAndAdd(results, true, true);

            yAxis.setTickUnit(Math.abs(yAxis.getUpperBound() - yAxis.getLowerBound()) / 15);
        }
        addTooltipForNodesInChart();
    }

    private void setUpBounds(List<SimulationResults> results) {
        for (SimulationResults simulationResults : results) {
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
            var newValue = (((100 - 0) * (simulationResults.getBestFitness().get(i) - lowerBound)) / (upperBound - lowerBound)) + 0;
            var data = new XYChart.Data<>(i, newValue);
            seriesBest.getData().add(data);
        }

        for (int i = 0; i < simulationResults.getAverageFitness().size(); i++) {
            var newValue = (((100 - 0) * (simulationResults.getAverageFitness().get(i) - lowerBound)) / (upperBound - lowerBound)) + 0;
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
        BaseController.savedDatasets = list;
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
