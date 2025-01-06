package com.gui;

import java.util.Optional;

import com.utils.Icons;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class Alerts {
  private static void setAlertIcon(Alert alert) {
    String imagePath = "src/main/java/com/icons/favicon.png";
    Icons.setIcon(
      (Stage) alert.getDialogPane().getScene().getWindow(), imagePath
    );
  }

  public static void showAlert(
    String title, String header, String content, AlertType type
  ) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    setAlertIcon(alert);
    alert.show();
  }

  public static Optional <ButtonType> showConfirmation(
    String title, String content
  ) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    setAlertIcon(alert);
    return alert.showAndWait();
  }
}
