package gui;

import javafx.scene.control.MenuItem;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import services.ClientService;
import services.ContractService;
import java.io.IOException;

public class ViewController {
  @FXML
  private MenuItem clientList;

  @FXML
  private MenuItem contractList;

  public void setStage(Stage stage) {}

  public void openClientList(ActionEvent event) {
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

  public void openContractList(ActionEvent event) {
    try {
      ContractService contractService = new ContractService();
      ClientService clientService = new ClientService();

      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("ContractListView.fxml"));

      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == ContractListController.class) {
          ContractListController controller = new ContractListController();
          controller.setContractService(contractService);
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

      Stage currentStage = (Stage)
        contractList.getParentPopup().getOwnerWindow();
      Scene newScene = new Scene(root);
      currentStage.setScene(newScene);
      currentStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
	}
}
