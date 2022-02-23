package controllers.components;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
