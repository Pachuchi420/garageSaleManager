package cc.pachuchi.garagesalemanager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;


import java.util.Objects;


public class ProgramLauncher extends Application {
    private Storage storage = new Storage();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("listItemsView.fxml")));
            Parent root = loader.load();

            // Get the controller from the FXMLLoader
            listItemsViewController controller = loader.getController();
            controller.setStorage(storage);

            // Create the scene and load the CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

            // Set up the stage
            stage.setScene(scene);
            stage.setTitle("Garage Sale Manager");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

        Platform.exit();
    }



}