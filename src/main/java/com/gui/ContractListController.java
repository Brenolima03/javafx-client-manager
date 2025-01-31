package com.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Contract;
import com.model.entities.Estate;
import com.model.entities.Guarantee;
import com.services.ClientService;
import com.services.ContractService;
import com.services.EstateService;
import com.utils.ContractBuilder;
import com.utils.Currency;
import com.utils.CustomContextMenu;
import com.utils.Date;
import com.utils.Icons;
import com.utils.TableCellConfiguration;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ContractListController {
  private int currentPage = 1;
  private final int pageSize = 10;
  private ClientService clientService;
  private EstateService estateService;
  private ContractService contractService;

  @FXML
  private TableView <Contract> contractTable;

  @FXML
  private TextField searchField;

  @FXML
  private Button searchButton;

  @FXML
  private ComboBox <String> filterCombobox;

  @FXML
  private Button addContractButton;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private Button applyDateButton;

  @FXML
  private Button downloadButton;

  @FXML
  private TableColumn <Contract, Integer> contractIdColumn;

  @FXML
  private TableColumn <Contract, String> tenantColumn;

  @FXML
  private TableColumn <Contract, String> landlordColumn;

  @FXML
  private TableColumn <Contract, LocalDate> rentBeginningColumn;

  @FXML
  private TableColumn <Contract, LocalDate> rentEndColumn;

  @FXML
  private TableColumn <Contract, String> rentValueColumn;

  @FXML
  private TableColumn <Contract, String> guaranteeTypeColumn;

  @FXML
  private TableColumn <Contract, String> depositColumn;

  @FXML
  private TableColumn <Contract, List <String>> guarantorsColumn;

  @FXML
  private TableColumn <Contract, String> downloadColumn;

  @FXML
  private Pagination pagination;

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }
  
  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  public void setContractService(ContractService contractService) {
    this.contractService = contractService;
  }

  private void setupFilterCombobox() {
    filterCombobox.setItems(
      FXCollections.observableArrayList(
        "Contrato", "Locatário", "Locador", "Valor do aluguel"
      )
    );
    filterCombobox.setValue("Contrato");
  }

  @FXML
  private void filterByDate() {
    if (startDatePicker.getValue() != null && endDatePicker.getValue() != null){
      String startDate = startDatePicker.getValue().toString();
      String endDate = endDatePicker.getValue().toString();

      List<Contract> contracts =
        contractService.getContractsByDate(startDate, endDate);

      if (contracts != null && !contracts.isEmpty())
        refreshTableData(
          FXCollections.observableArrayList(contracts)
        );
      else {
        contractTable.setItems(
          FXCollections.observableArrayList(contractService.getAllContracts())
        );
        Alerts.showAlert(
          null, "Nenhum contrato encontrado", null, AlertType.WARNING
        );
      }
    }
  }

  @FXML
  private void downloadContracts() {
    boolean allContractsEmitted = true;
    StringBuilder failedContractIds = new StringBuilder();

    // Iterate over all contracts in the TableView
    for (Contract contract : contractTable.getItems()) {
      if (contract != null) {
        int tenantId = contract.getTenant();
        int landlordId = contract.getLandlord();
        int estateId = contract.getEstate();

        Client tenantObj = clientService.findClientById(tenantId);
        Client landlordObj = clientService.findClientById(landlordId);
        Estate estateObj = estateService.findState(estateId);

        try {
          // Emit the contract for each contract in the table
          ContractBuilder.emitContract(
            tenantObj, landlordObj, estateObj, contract
          );
        } catch (Exception e) {
          // If emission fails, mark the operation as failed
          // and store the contract ID
          allContractsEmitted = false;
          failedContractIds.append("Contrato ID: ")
            .append(contract.getId()).append("\n");
        }
      }
    }

    // Show success alert only if all contracts were emitted successfully
    if (allContractsEmitted) {
      Alerts.showAlert(
        "Sucesso", "Todos os contratos foram emitidos com sucesso!",
        null, AlertType.INFORMATION
      );
    } else {
      // Show error alert with the failed contract IDs
      Alerts.showAlert(
        "Erro",
        "Falha ao emitir os seguintes contratos:\n" +
        failedContractIds.toString(),
        "Entre em contato com o suporte para mais informações.", AlertType.ERROR
      );
    }
  }

  private void setupActionColumn() {
    downloadColumn.setCellFactory(param -> new TableCell<Contract, String>() {
      private final Button downloadButton = new Button();
      private final HBox actionBox = new HBox(19, downloadButton);

      {
        Icons.setupTransparentButton(
          downloadButton, "icons/download.png", event -> {
            Contract contract = getTableRow().getItem();
            if (contract != null) {
              int tenantId = contract.getTenant();
              int landlordId = contract.getLandlord();
              int estateId = contract.getEstate();

              Client tenantObj = clientService.findClientById(tenantId);
              Client landlordObj = clientService.findClientById(landlordId);
              Estate estateObj = estateService.findState(estateId);

              try {
                ContractBuilder.emitContract(
                  tenantObj, landlordObj, estateObj, contract
                );

                Alerts.showAlert(
                  "Sucesso", "Contrato emitido com sucesso!",
                  null, AlertType.INFORMATION
                );
              } catch (Exception e) {
                Alerts.showAlert(
                  "Erro",
                  "Falha ao emitir o contrato. Entre em contato com o suporte.",
                  e.getMessage(), AlertType.ERROR
                );
              }
            }
          });
        actionBox.setAlignment(Pos.CENTER);
      }
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : actionBox);
      }
    });
  }

  public void refreshTableData(ObservableList<Contract> contractList) {
    contractTable.getItems().clear();
    contractTable.setItems(contractList);
    setupActionColumn();
  }

  public void setupPagination(ObservableList<Contract> contractList) {
    int totalContracts = contractList.size();
    int pageCount = (totalContracts + pageSize - 1) / pageSize;

    pagination.setPageCount(pageCount);
    pagination.setVisible(pageCount > 1);
    pagination.setCurrentPageIndex(currentPage - 1);
    pagination.setMaxPageIndicatorCount(5);

    // Set up the page factory to update the table based on selected page
    pagination.setPageFactory(pageIndex -> {
      currentPage = pageIndex + 1;

      // Calculate the sublist for the current page
      int fromIndex = (currentPage - 1) * pageSize;
      int toIndex = Math.min(fromIndex + pageSize, totalContracts);
      List<Contract> contractsForPage =
        contractList.subList(fromIndex, toIndex);

      refreshTableData(FXCollections.observableArrayList(contractsForPage));
      return new Label("");
    });
  }

  private void handleSearch() {
    String filter = filterCombobox.getSelectionModel().getSelectedItem();
    String argument = searchField.getText().trim().replaceAll("[()\\-./]", "");

    if (filter == null || filter.isEmpty()) {
      Alerts.showAlert(
        null, "Selecione o filtro apropriado.", null, AlertType.WARNING
      );
      return;
    }

    try {
      List<Contract> contracts = argument.isEmpty() 
        ? contractService.getAllContracts() 
        : contractService.search(filter, argument);

      if (contracts == null || contracts.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum contrato encontrado", null, AlertType.WARNING
        );
        contracts = contractService.getAllContracts();
      }

      setupPagination(FXCollections.observableArrayList(contracts));
      searchField.clear();
    } catch (DbException e) {
      Alerts.showAlert("Erro ao buscar", e.getMessage(), null, AlertType.ERROR);
    }
  }

  private void openNewContractForm() {
    try {
      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/com/gui/NewContractForm.fxml")
      );
      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == NewContractFormController.class) {
          NewContractFormController controller =
            new NewContractFormController();

          controller.setContractService(new ContractService());
          controller.setContractController(this);
          controller.setClientService(new ClientService());
          controller.setEstateService(new EstateService());

          return controller;
        }
        try {
          return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
          System.err.println(e.getMessage());
        }
        return null;
      });

      Parent root = loader.load();

      NewContractFormController controller = loader.getController();
      Stage newContractStage = new Stage();
      controller.setStage(newContractStage);

      newContractStage.setTitle("Adicionar contrato");
      newContractStage.setScene(new Scene(root));
      newContractStage.setWidth(760);
      newContractStage.setHeight(920);

      // Show the new contract form stage
      newContractStage.show();
      newContractStage.setResizable(false);
    } catch (IOException e) {
      Alerts.showAlert(
        "Erro", "Falha ao abrir o formulário. Entre em contato com o suporte.",
        e.getMessage(), AlertType.ERROR
      );
    }
  }

  private void populateCells(
    CustomContextMenu contextMenu, TableCellConfiguration tableCellConfig
  ) {
    tableCellConfig.configureCellFactory(
      contractIdColumn, 100, "id", contextMenu
    );
    tableCellConfig.configureCellFactory(
      tenantColumn, 225, "tenant", contextMenu
    );
    tableCellConfig.configureCellFactory(
      landlordColumn, 225, "landlord", contextMenu
    );
    tableCellConfig.configureCellFactory(
      rentBeginningColumn, 200, "rentBeginning", contextMenu
    );
    tableCellConfig.configureCellFactory(
      rentEndColumn, 200, "rentEnd", contextMenu
    );
    tableCellConfig.configureCellFactory(
      rentValueColumn, 160, "rentValue", contextMenu
    );
    tableCellConfig.configureCellFactory(
      guaranteeTypeColumn, 220, "guarantee", contextMenu
    );
    tableCellConfig.configureCellFactory(
      depositColumn, 160, "deposit", contextMenu
    );
    tableCellConfig.configureCellFactory(
      guarantorsColumn, 220, "guarantors", contextMenu
    );

    TableCellConfiguration.setupComboBoxColumn(
      contractTable, guarantorsColumn, "transparent-combobox", col -> {
      List<String> guarantors = contractService.getGuarantors(col.getId());

      return guarantors != null && !guarantors.isEmpty() ?
        guarantors : Collections.emptyList();
    });

    tenantColumn.setCellValueFactory(cellData -> {
      return new SimpleStringProperty(
        contractService.getClient(cellData.getValue().getTenant())
      );
    });

    landlordColumn.setCellValueFactory(cellData -> {
      return new SimpleStringProperty(
        contractService.getClient(cellData.getValue().getLandlord())
      );
    });

    rentValueColumn.setCellValueFactory(cellData -> {
      double rent = cellData.getValue().getRentValue();
      return new SimpleStringProperty(
        rent > 0 ? Currency.getCurrencyConverter().toString(rent) : ""
      );
    });

    depositColumn.setCellValueFactory(cellData -> {
      double deposit = cellData.getValue().getDepositValue();
      return new SimpleStringProperty(
        deposit > 0 ? Currency.getCurrencyConverter().toString(deposit) : ""
      );
    });

    guaranteeTypeColumn.setCellValueFactory(cellData -> {
      Guarantee guarantee = cellData.getValue().getGuarantee();
      String formattedGuarantee = guarantee.getGuaranteeType().toString();
      return new SimpleStringProperty(formattedGuarantee);
    });
  }

  public void initialize() {
    try {
      if (clientService == null)
        throw new IllegalStateException(
          "ClientService was not initialized. Call setClientService() " +
          "before loading the controller."
        );

      Icons.setupTransparentButton(
        searchButton, "icons/search-icon.png",
        event -> handleSearch()
      );
      Icons.setupTransparentButton(
        addContractButton, "icons/add-icon.png",
        event -> openNewContractForm()
      );
      Icons.setupTransparentButton(
        downloadButton, "icons/download.png",
        event -> downloadContracts()
      );

      contractTable.setPlaceholder(new Label("Não há clientes registrados"));

      CustomContextMenu contextMenu = new CustomContextMenu();
      TableCellConfiguration tableCellConfig = new TableCellConfiguration();

      searchField.focusedProperty().addListener(
      (observable, oldValue, newValue) -> {
        if (newValue)
          contextMenu.setCustomContextMenuForTextFields(searchField);
      });
      startDatePicker.getEditor().focusedProperty().addListener(
      (observable, oldValue, newValue) -> {
        if (newValue)
          contextMenu
            .setCustomContextMenuForTextFields(startDatePicker.getEditor());
      });
      endDatePicker.getEditor().focusedProperty().addListener(
      (observable, oldValue, newValue) -> {
        if (newValue)
          contextMenu
            .setCustomContextMenuForTextFields(endDatePicker.getEditor());
      });

      Date.applyDateMaskOnInputFields(startDatePicker);
      Date.applyDateMaskOnInputFields(endDatePicker);

      setupFilterCombobox();
      setupPagination(
        FXCollections.observableArrayList(contractService.getAllContracts())
      );
      populateCells(contextMenu, tableCellConfig);
    } catch (IllegalStateException e) {
      Alerts.showAlert(
        "Erro ao abrir página ",
        e.getMessage(), null, AlertType.ERROR
      );
    }
  }
}
