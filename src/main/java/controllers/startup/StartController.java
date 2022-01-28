package controllers.startup;

import controllers.base.BaseController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class StartController extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/mainPage.fxml")));
        stage.setScene(new Scene(root));
        BaseController.mainStage = stage;
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}