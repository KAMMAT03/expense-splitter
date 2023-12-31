package pl.edu.pw.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ExpenseSplitterApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ExpenseSplitterApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),788,551);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }
}