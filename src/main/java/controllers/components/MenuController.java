package controllers.components;

import controllers.base.BaseController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.utils.TextFormattersFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * Controller that provies utility for menu. All controller extends this controller.
 */
public class MenuController {
    @FXML public Slider speedChangerMenu;
    @FXML public TextField randomSeed;
    @FXML public MenuItem customMenu;

    /**
     * Initializes menu.
     */
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
            randomSeed.setTextFormatter(TextFormattersFactory.makeIntegerFormatter());
            randomSeed.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(""))
                    BaseController.randomSeed = Integer.valueOf(newValue);
            });
        }
    }

    /**
     * Closes app.
     */
    public void closeApp() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Opens doc with instructions.
     */
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

    /**
     * Shows a window with iformation about application.
     * @throws IOException
     */
    public void about() throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/components/aboutProgram.fxml")));
        stage.setScene(new Scene(root));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon_blank.png"))));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    /**
     * Randomizes seed for generator.
     */
    public void randomSeed() {
        randomSeed.setText(BaseController.randomGenerator.nextInt(100000)+"");
    }
}
