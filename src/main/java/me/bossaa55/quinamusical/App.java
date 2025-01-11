package me.bossaa55.quinamusical;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/icon.png"))));

        Font.loadFont(getClass().getResourceAsStream("/fonts/OpenSans-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/OpenSans-Bold.ttf"), 14);

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("QuinesManager.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1440, 1024*0.8);
        stage.setTitle("Quina Musical");
        stage.setScene(scene);
        //stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}