package application;

import java.io.IOException;
import gui.ViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.Icons;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    try {
      FXMLLoader loader =
      new FXMLLoader(getClass().getResource("/gui/View.fxml"));
      Icons.setIcon(primaryStage, "src/icons/favicon.png");
      Parent root = loader.load();
      ViewController controller = loader.getController();
      controller.setStage(primaryStage);

      Scene scene = new Scene(root);
      primaryStage.setScene(scene);
      primaryStage.setTitle("Gestão");
      primaryStage.setResizable(false);
      primaryStage.setWidth(1920);
      primaryStage.setHeight(1080);
      primaryStage.setMaximized(true);
      primaryStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
