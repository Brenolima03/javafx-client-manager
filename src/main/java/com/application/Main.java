package com.application;

import java.io.IOException;

import com.gui.Alerts;
import com.gui.ViewController;
import com.utils.Icons;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    try {
      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/com/gui/View.fxml"));
      Icons.setIcon(primaryStage, "icons/favicon.png");
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
      Alerts.showAlert(
        "Erro ao iniciar o sistema",e.getMessage(), null, AlertType.ERROR
      );
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
