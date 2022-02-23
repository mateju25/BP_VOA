package controllers.components;

import controllers.base.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import model.utils.SimulationResults;

import java.io.IOException;

public class DatasetPartController extends ListCell<SimulationResults> {
    public AnchorPane pane;
    public Label lblNumber;
    public Label lblName;
    public CheckBox checkBest;
    public CheckBox checkAverage;
    public Button btnDelete;
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