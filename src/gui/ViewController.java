package gui;

import javafx.scene.control.MenuItem;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import services.ClientService;
import java.io.IOException;

public class ViewController {
  @FXML
  private MenuItem clientList;
  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void onListClientsAction(ActionEvent event) {
    try {
      ClientService clientService = new ClientService();

      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("ClientListView.fxml")
      );

      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == ClientListController.class) {
          ClientListController controller = new ClientListController();
          controller.setClientService(clientService);
          return controller;
        }
        try {
          return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      });
      Parent root = loader.load();
      Stage currentStage = (Stage) clientList.getParentPopup().getOwnerWindow();
      Scene newScene = new Scene(root);

      currentStage.setScene(newScene);
      currentStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
