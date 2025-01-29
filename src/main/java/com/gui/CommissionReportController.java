package com.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.model.entities.Client;
import com.services.ClientService;
import com.services.ContractService;
import com.utils.CommissionReportBuilder;
import com.utils.Currency;
import com.utils.Icons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class CommissionReportController {
  private Stage stage;
  private ContractService contractService;
  private ClientService clientService;

  @FXML
  private TableView<Map.Entry<String, String>> commissionTable;

  @FXML
  private TableColumn<Map.Entry<String, String>, String> landlordColumn;

  @FXML
  private TableColumn<Map.Entry<String, String>, String> commissionValueColumn;

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "icons/favicon.png");
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setContractService(ContractService contractService) {
    this.contractService = contractService;
  }

  public void initialize() {
    try {
      if (contractService == null)
        throw new IllegalStateException(
          "ContractService was not initialized. Call setContractService() " +
          "before loading the controller."
        );

      commissionTable.setPlaceholder(new Label("Não há comissões"));
      commissionTable.setPrefHeight(460);

      landlordColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getKey())
      );

      commissionValueColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getValue())
        );

      landlordColumn.setPrefWidth(210);
      commissionValueColumn.setPrefWidth(210);

      loadRentAndCommission();
    } catch (IllegalStateException e) {
      Alerts.showAlert(
        "Erro ao abrir página ",
        e.getMessage(), null, AlertType.ERROR
      );
    }
  }

  private void loadRentAndCommission() {
    try {
      var contracts = contractService.getAllContracts();
      if (contracts == null) return;

      double totalCommission = 0.0;
      LinkedHashMap<String, String> map = new LinkedHashMap<>();

      for (var contract : contracts) {
        double rentValue = contract.getRentValue();
        Client landlordObj = clientService.findClientById(contract.getId());
        String landlordName = landlordObj.getName();
        double commission = rentValue * 0.10;
        totalCommission += commission;

        map.put(
          landlordName, Currency.getCurrencyConverter().toString(commission)
        );
      }

      map.put(
        "Total", Currency.getCurrencyConverter().toString(totalCommission)
      );

      // Convert the map entries to an observable list
      ObservableList<Map.Entry<String, String>> data =
        FXCollections.observableArrayList(map.entrySet());

      commissionTable.setItems(data);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  @FXML
  private void handleGenerateExcelReport() {
    Map<String, String> commissionData = new LinkedHashMap<>();

    for (Map.Entry<String, String> entry : commissionTable.getItems())
      commissionData.put(entry.getKey(), entry.getValue());

    CommissionReportBuilder reportBuilder = new CommissionReportBuilder();
    reportBuilder.generateReportFile(commissionData);
  }
}
