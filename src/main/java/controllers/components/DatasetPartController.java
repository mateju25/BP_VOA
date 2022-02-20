package controllers.components;

import controllers.base.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import model.utils.SimulationResults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatasetPartController extends ListCell<SimulationResults> {
    public AnchorPane pane;
    public Label lblNumber;
    public Label lblName;
    public CheckBox checkBest;
    public CheckBox checkAverage;
    public Button btnDelete;
    public Button btnSave;
    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(SimulationResults emp, boolean empty) {
        super.updateItem(emp, empty);

        if(empty || emp == null) {

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
            controller.lblNumber.setText(emp.getNumberOfDataset()+"");
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
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.csv"));
            controller.btnSave.setOnAction(event -> {
                var formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
                fileChooser.setInitialFileName(BaseController.chosenAlgorithm.nameForFaces().chars().filter(Character::isUpperCase)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString() + "_" + LocalDateTime.now().format(formatter));
                try {
                    var file = fileChooser.showSaveDialog(BaseController.mainStage);
                    if (file != null) {
                        emp.writeToCsv(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            setText(null);
            setGraphic(controller.pane);
        }

    }
}