package controllers.components;

import controllers.base.BaseController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class MenuController {
    @FXML public Slider speedChangerMenu;
    @FXML public TextField randomSeed;

    public void initMenu() {
        if (speedChangerMenu != null) {
            speedChangerMenu.valueProperty().addListener(new ChangeListener<>() {
                @Override
                public void changed(
                        ObservableValue<? extends Number> observableValue,
                        Number oldValue,
                        Number newValue) {

                    BaseController.simulationSpeed = newValue.intValue();
                }

            });
            randomSeed.textProperty().addListener((observable, oldValue, newValue) -> {
                BaseController.randomSeed = Integer.valueOf(newValue);
            });
        }
    }

    public void closeApp() {
        Platform.exit();
        System.exit(0);
    }

    public void openDoc() {
        if (Desktop.isDesktopSupported()) {
            try {
                URL url = getClass().getResource("/pdfs/blank.pdf");
                File myFile = new File(url.toURI());
                Desktop.getDesktop().open(myFile);
            } catch (IOException | URISyntaxException ignored) {
            }
        }
    }

    public void about() throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/components/aboutProgram.fxml")));
        stage.setScene(new Scene(root));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon_blank.png"))));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
