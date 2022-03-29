package controllers.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.components.CrossHairLineChart;
import controllers.components.DatasetPartController;
import controllers.components.MenuController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

/**
 * Controller that provides functionality for visualization page.
 */
public class VisualizationController extends MenuController {
    @FXML
    public Label heading;
    @FXML
    public Pane algoPane;
    public CrossHairLineChart<Integer, Double> chart;
    public NumberAxis xAxis = new NumberAxis();
    public NumberAxis yAxis = new NumberAxis();
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

    /**
     * Initializes visualization page view. Loads all saved datasets.
     * @throws JsonProcessingException
     */
    public void initialize() throws JsonProcessingException {
        speedChangerMenu.adjustValue(BaseController.simulationSpeed);
        initMenu();

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
                ObjectMapper mapper = new ObjectMapper();
                simulationResults.setUsedAlgorithmInJson(mapper.writeValueAsString(BaseController.chosenAlgorithm));
                simulationResults.setUsedProblemInJson(mapper.writeValueAsString(BaseController.chosenProblem));
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

    /**
     * Add tooltip for all points in chart.
     */
    private void addTooltipForNodesInChart() {
        for (XYChart.Series<Integer, Double> s : chart.getData()) {
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

    /**
     * Setup initial parameters for axis.
     */
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

    /**
     * Creates custom chart.
     * @param toggleButton Button that property of selection is used to display ruler.
     */
    private void createChart(ToggleButton toggleButton) {
        chart = new CrossHairLineChart<Integer, Double>((Axis) xAxis, (Axis) yAxis, toggleButton);
        chart.setLayoutY(11);
        chart.setPrefHeight(527.0);
        chart.setPrefWidth(1031.0);
        chart.setAnimated(false);
        algoPane.getChildren().add(chart);
    }

    /**
     * Creates ruler button.
     * @return toggleable button.
     */
    private ToggleButton createToggleButton() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setLayoutX(347.0);
        toggleButton.setLayoutY(20.0);
        toggleButton.setPrefHeight(34);
        toggleButton.setPrefWidth(34);
        toggleButton.getStyleClass().add("button-black");
        toggleButton.setFocusTraversable(false);
        toggleButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ruler.png")))));
        anchorPane.getChildren().add(toggleButton);
        return toggleButton;
    }

    /**
     * Go back to main page.
     * @throws IOException
     */
    public void goBack() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        BaseController.mainStage.setScene(new Scene(root));
        BaseController.mainStage.show();
    }

    /**
     * Loads dataset into list of simulation runs.
     */
    public void addDataset() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        var files = fileChooser.showOpenMultipleDialog(BaseController.mainStage.getOwner());
        if (files == null) {
            BaseController.showInfo(infoBox, infoBoxLabel, "No file selected!");
            return;
        }
        for (File file : files) {
            var results = new SimulationResults();

            try {
//                results.loadFromCsv(file);
                results.loadFromJson(file);
            } catch (Exception e) {
                BaseController.showInfo(infoBox, infoBoxLabel, "There was an error loading file!");
                return;
            }
            results.setNumberOfDataset(listView.getItems().stream().mapToInt(SimulationResults::getNumberOfDataset).max().orElse(0) + 1);
            listView.getItems().add(results);
            if (BaseController.savedDatasets == null)
                BaseController.savedDatasets = new ArrayList<>();
            BaseController.savedDatasets.add(results);

            upperBound = 0.0;
            lowerBound = 1000000.0;
            setUpBounds(BaseController.savedDatasets);

            chart.getData().clear();
            for (SimulationResults simulationResults : BaseController.savedDatasets) {
                scaleEverythingAndAdd(simulationResults, true, true);
            }

            yAxis.setTickUnit(Math.abs(yAxis.getUpperBound() - yAxis.getLowerBound()) / 15);
        }
        addTooltipForNodesInChart();
    }

    /**
     * Setup boundaries for scaling.
     * @param results all simulation results.
     */
    private void setUpBounds(List<SimulationResults> results) {
        for (SimulationResults simulationResults : results) {
            if (simulationResults.getLowerBound() < lowerBound)
                lowerBound = simulationResults.getLowerBound();
            if (simulationResults.getUpperBound() > upperBound)
                upperBound = simulationResults.getUpperBound();
        }
    }

    /**
     * Scales every point in chart to scal 0 - 100.
     * @param simulationResults one item from simulation results.
     * @param useBest flag if best curve is showed.
     * @param useAverage flag if average curve is showed.
     */
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

    /**
     * Event listener for change in list view
     */
    public void somethingChanged() {
        chart.getData().clear();
        var list = new ArrayList<SimulationResults>();
        upperBound = 0.0;
        lowerBound = 1000000.0;
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
        addTooltipForNodesInChart();

        BaseController.savedDatasets = list;
        listView.getItems().setAll(list);
    }

    /**
     * Exports picture of chart
     * @throws IOException
     */
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
