package com.gui;

import java.io.IOException;

import com.services.ClientService;
import com.services.ContractService;
import com.services.EstateService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class ViewController {
  @FXML
  private MenuItem clientList;

  @FXML
  private MenuItem contractList;

  @FXML
  private MenuItem estateList;

  @FXML
  private MenuItem commissionReport;

  @FXML
  private MenuItem test;

  public void setStage(Stage stage) {
  }

  public void openClientList(ActionEvent event) {
    try {
      ClientService clientService = new ClientService();

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("ClientListView.fxml"));

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
      currentStage.setTitle("Clientes");
      currentStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void openEstateList(ActionEvent event) {
    try {
      EstateService estateService = new EstateService();
      ClientService clientService = new ClientService();

      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("EstateListView.fxml"));

      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == EstateListController.class) {
          EstateListController controller = new EstateListController();
          controller.setEstateService(estateService);
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

      Stage currentStage = (Stage) estateList.getParentPopup().getOwnerWindow();
      Scene newScene = new Scene(root);
      currentStage.setScene(newScene);
      currentStage.setTitle("Imóveis");
      currentStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void openContractList(ActionEvent event) {
    try {
      ContractService contractService = new ContractService();
      ClientService clientService = new ClientService();
      EstateService estateService = new EstateService();

      FXMLLoader loader =
        new FXMLLoader(getClass().getResource("ContractListView.fxml"));

      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == ContractListController.class) {
          ContractListController controller = new ContractListController();
          controller.setContractService(contractService);
          controller.setClientService(clientService);
          controller.setEstateService(estateService);
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

      Stage currentStage =
        (Stage) contractList.getParentPopup().getOwnerWindow();
      Scene newScene = new Scene(root);
      currentStage.setScene(newScene);
      currentStage.setTitle("Contratos");
      currentStage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void openComissionReport() {
    ClientService clientService = new ClientService();
    try {
      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/com/gui/CommissionReportView.fxml")
      );

      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == CommissionReportController.class) {
          CommissionReportController controller =
            new CommissionReportController();
          controller.setContractService(new ContractService());
          controller.setClientService(clientService);
          return controller;
        }
        try {
          return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      });

      Parent root = loader.load();

      CommissionReportController controller = loader.getController();
      Stage commissionReportStage = new Stage();
      controller.setStage(commissionReportStage);

      commissionReportStage.setTitle("Relatório de Comissões");
      commissionReportStage.setScene(new Scene(root));
      commissionReportStage.setWidth(440);
      commissionReportStage.setHeight(560);

      commissionReportStage.show();
      commissionReportStage.setResizable(false);
    } catch (IOException e) {
      e.printStackTrace();
      Alerts.showAlert(
        "Erro", "Falha ao abrir o relatório de comissões. " +
        "Entre em contato com o suporte.", e.getMessage(), AlertType.ERROR
      );
    }
  }
}
