package controllers.components;

import controllers.base.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import model.utils.SimulationResults;

import java.io.IOException;

public class DatasetPartController extends ListCell<SimulationResults> {
    public AnchorPane pane;
    public Label lblNumber;
    public Label lblName;
    public CheckBox checkBest;
    public CheckBox checkAverage;
    public Button btnDelete;
    public Tooltip tooltip;
    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(SimulationResults emp, boolean empty) {
        super.updateItem(emp, empty);

        if (empty || emp == null) {

            setText(null);
            setGraphic(null);

        } else {

            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/views/components/datasetPart.fxml"));
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            DatasetPartController controller = mLLoader.getController();
            controller.lblNumber.setText(emp.getNumberOfDataset() + "");
            controller.lblName.setText(emp.getNameOfDataset());
            controller.tooltip.setText("Algorithm parameters: " + emp.getUsedAlgorithmInJson() + "\n\n" +
                    "Problem parameters: " + emp.getUsedProblemInJson());
            controller.tooltip.setShowDelay(Duration.millis(10));
            controller.tooltip.setShowDuration(Duration.minutes(1));

            controller.btnDelete.setOnAction(event -> {
                emp.setDeleted(true);
                BaseController.visualizationController.somethingChanged();
            });
            controller.checkAverage.setOnAction(event -> {
                emp.setShowAverage(controller.checkAverage.isSelected());
                BaseController.visualizationController.somethingChanged();
            });
            controller.checkBest.setOnAction(event -> {
                emp.setShowBest(controller.checkBest.isSelected());
                BaseController.visualizationController.somethingChanged();
            });
            setText(null);
            setGraphic(controller.pane);
        }

    }
}