package gui;

import java.util.Optional;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.Icons;

public class Alerts {
  private static void setAlertIcon(Alert alert) {
    String imagePath = "src/icons/favicon.png";
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
