package com.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Contract;
import com.model.entities.Estate;
import com.model.entities.Guarantee.GuaranteeType;
import com.services.ClientService;
import com.services.ContractService;
import com.services.EstateService;
import com.utils.ContractBuilder;
import com.utils.Currency;
import com.utils.Icons;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ContractListController {
  private int currentPage = 1;
  private final int pageSize = 10;
  private ClientService clientService;
  private EstateService estateService;

  @FXML
  private TableView <Contract> contractTable;

  @FXML
  private TextField searchField;

  @FXML
  private Button searchButton;

  @FXML
  private ComboBox <String> filtersCombobox;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private Button applyDateButton;

  @FXML
  private Button addContractButton;

  @FXML
  private Button downloadButton;

  @FXML
  private TableColumn <Contract, Integer> contractIdColumn;

  @FXML
  private TableColumn <Contract, String> landlordColumn;

  @FXML
  private TableColumn <Contract, String> tenantColumn;

  @FXML
  private TableColumn <Contract, LocalDate> rentBeginningColumn;

  @FXML
  private TableColumn <Contract, LocalDate> rentEndColumn;

  @FXML
  private TableColumn <Contract, Double> rentValueColumn;

  @FXML
  private TableColumn <Contract, String> guaranteeTypeColumn;

  @FXML
  private TableColumn <Contract, List <String>> guarantorColumn;

  @FXML
  private TableColumn <Contract, String> downloadColumn;

  @FXML
  private Pagination pagination;

  private Stage currentStage;

  private ContractService contractService;

  public void setStage(Stage stage) {
    this.currentStage = stage;
  }

  public void setContractService(ContractService contractService) {
    this.contractService = contractService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  private void setupFilterCombobox() {
    filtersCombobox.setItems(
      FXCollections.observableArrayList(
        "Contrato", "Locatário", "Locador", "Valor do aluguel"
      )
    );
    filtersCombobox.setValue("Contrato");
  }

  @FXML
  public void openContractList(ActionEvent event) {
    if (currentStage == null) {
      System.err.println(
        "Error: Current stage is not set. Ensure setStage() is called."
      );
      return;
    }

    try {
      VBox root = new VBox();
      root.getChildren().add(new Label("Contract List View"));

      Scene scene = new Scene(root);
      currentStage.setScene(scene);

      currentStage.show();
    } catch (Exception e) {}
  }

  private void blockTypingExceptCtrlC(Node node) {
    node.addEventFilter(KeyEvent.ANY, event -> {
      if (!(event.isControlDown() && event.getCode() == KeyCode.C))
        event.consume();
    });
  }

  private void setupButton(
    Button button, String path,
    EventHandler <ActionEvent> event, boolean isTransparent
  ) {
    Icons.setButtonIcon(button, path);
    button.setPrefWidth(38);
    button.setPrefHeight(38);
    button.setOnAction(event);
    if (isTransparent)
      button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
  }

  private void searchContracts() {
    String filter = filtersCombobox.getSelectionModel().getSelectedItem();
    String searchText = searchField.getText().trim();

    if (filter == null || filter.isEmpty()) {
      Alerts.showAlert(
        null, "Selecione o filtro apropriado.",
        null, AlertType.ERROR
      );
      return;
    }

    try {
      List <Contract> contracts =
        searchText.isEmpty() ?
        contractService.findPaginated(currentPage = 1, pageSize) :
        contractService.search(filter, searchText);

      if (contracts == null || contracts.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum contrato encontrado",
          null, AlertType.ERROR
        );
        return;
      }

      int totalContracts = searchText.isEmpty() ?
        contractService.count() : contracts.size();
      int pageCount = (totalContracts + pageSize - 1) / pageSize;

      pagination.setPageCount(pageCount);
      pagination.setVisible(pageCount > 1);
      pagination.setCurrentPageIndex(Math.min(currentPage, pageCount) - 1);

      contractTable.getSortOrder().clear();
      Platform.runLater(() -> {
        contractTable.getItems().setAll(contracts);
      });

    } catch (DbException e) {
      Alerts.showAlert(
        "Erro ao buscar ", e.getMessage(), null, AlertType.ERROR
      );
    }
  }

  @FXML
  private void filterByDate() {
    if (startDatePicker.getValue() != null && endDatePicker.getValue() != null){
      String startDate = startDatePicker.getValue().toString();
      String endDate = endDatePicker.getValue().toString();

      List<Contract> contracts =
        contractService.getContractsByDate(startDate, endDate);

      if (contracts != null && !contracts.isEmpty()) refreshTableData();
      else contractTable.setItems(FXCollections.observableArrayList());
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

  private ObservableList <Contract> getContractData(int page) {
    try {
      return FXCollections.observableArrayList(
        contractService.findPaginated(page, pageSize)
      );
    } catch (Exception e) {
      return FXCollections.observableArrayList();
    }
  }

  public void refreshTableData() {
    contractTable.setItems(getContractData(currentPage));
    setupActionColumn();
  }

  public void setupPagination() {
    int totalContracts = contractService.count();
    int pageCount = (totalContracts + pageSize - 1) / pageSize;

    pagination.setPageCount(pageCount);
    // Show pagination only if more than one page
    pagination.setVisible(pageCount > 1);
    pagination.setCurrentPageIndex(currentPage - 1);
    // Display 5 pages at once
    pagination.setMaxPageIndicatorCount(5);
    // Set up the page factory to update the table based on selected page
    pagination.setPageFactory(pageIndex -> {
      currentPage = pageIndex + 1;
      refreshTableData();
      return new Label("");
    });
  }

  private <T> void setupCell(TableColumn <Contract, T> column, double width) {
    column.setPrefWidth(width);
    column.setCellFactory(param -> new TableCell <> () {
      private final TextField textField = new TextField();
      {
        textField.setStyle(
          "-fx-background-color: transparent; -fx-alignment: center-left;"
        );
      }
    });
  }

  private <T> void configureNonEditableColumn(
    TableColumn <Contract, T> column,
    String property
  ) {
    column.setCellValueFactory(new PropertyValueFactory <> (property));
    column.setCellFactory(tc -> {
      TextFieldTableCell <Contract, T > cell =
      new TextFieldTableCell <> (new StringConverter <T> () {
        @Override
        public String toString(T object) {
          return object == null ? "" : object.toString();
        }

        @Override
        public T fromString(String string) {
          return null;
        }
      });

      blockTypingExceptCtrlC(cell);

      return cell;
    });
  }

  private void configureColumn() {
    configureNonEditableColumn(contractIdColumn, "id");
    configureNonEditableColumn(tenantColumn, "tenant");
    configureNonEditableColumn(landlordColumn, "landlord");
    configureNonEditableColumn(rentBeginningColumn, "rentBeginning");
    configureNonEditableColumn(rentEndColumn, "rentEnd");

    tenantColumn.setCellValueFactory(col -> {
      Contract contract = col.getValue();
      Client tenantObj = clientService.findClientById(contract.getTenant());
      String tenantName = tenantObj.getName();
      return new SimpleStringProperty(tenantName);
    });

    landlordColumn.setCellValueFactory(col -> {
      Contract contract = col.getValue();
      Client landlordObj = clientService.findClientById(contract.getLandlord());
      String landlordName = landlordObj.getName();
      return new SimpleStringProperty(landlordName);
    });
    rentBeginningColumn.setCellFactory(col -> {
      TextFieldTableCell <Contract, LocalDate> cell =
      new TextFieldTableCell <> (new StringConverter <LocalDate> () {
        private final DateTimeFormatter dateFormatter =
          DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public String toString(LocalDate object) {
          return object == null ? "" : object.format(dateFormatter);
        }

        @Override
        public LocalDate fromString(String string) {
          return null;
        }
      });
      blockTypingExceptCtrlC(cell);
      return cell;
    });
    rentEndColumn.setCellFactory(col -> {
      TextFieldTableCell <Contract, LocalDate> cell =
      new TextFieldTableCell <> (new StringConverter <LocalDate> () {
        private final DateTimeFormatter dateFormatter =
          DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public String toString(LocalDate object) {
          return object == null ? "" : object.format(dateFormatter);
        }

        @Override
        public LocalDate fromString(String string) {
          return null;
        }
      });
      blockTypingExceptCtrlC(cell);
      return cell;
    });
    rentValueColumn.setCellValueFactory(tc ->
    new SimpleObjectProperty<Double>(
        tc.getValue().getRentValue() != 0 ? tc.getValue().getRentValue() : null
      )
    );

    // Set the cell factory to allow editing and format the value as currency
    rentValueColumn.setCellFactory(
      tc -> new TextFieldTableCell <Contract, Double> (
        new StringConverter <Double> () {
      @Override
      public String toString(Double object) {
        return object != null ?
        Currency.getCurrencyConverter().toString(object) : "";
      }

      @Override
      public Double fromString(String string) {
        try {
          return
            Double.valueOf(string.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
          return 0.0;
        }
      }
    }) {
      @Override
      public void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);
        setText(
          empty || item == null ?
          null : Currency.getCurrencyConverter().toString(item)
        );
      }
    });

    guaranteeTypeColumn.setCellValueFactory(cellData -> {
      String guaranteeType = "";
      int contract = cellData.getValue().getId();
      guaranteeType = clientService.getGuaranteeTypeByContractId(contract);
      return new SimpleStringProperty(guaranteeType);
    });
    guaranteeTypeColumn.setCellFactory(tc -> {
      TextFieldTableCell <Contract, String> cell =
      new TextFieldTableCell <> (new StringConverter <String> () {
        @Override
        public String toString(String object) {
          if (object == null) return "";
          try {
            return GuaranteeType.valueOf(object).toString();
          } catch (IllegalArgumentException e) {
            return object; // Fallback for invalid input
          }
        }

        @Override
        public String fromString(String string) {
          return string;
        }
      });
      blockTypingExceptCtrlC(cell);
      return cell;
    });

    setupComboBoxColumn(guarantorColumn, "transparent-combobox",
    col -> {
      int contract = col.getId();
      List <String> guarantors = clientService.getGuarantorsById(contract);
      return guarantors;
    });
  }
  private void setupComboBoxColumn(
    TableColumn <Contract, List <String>> column, String styleClass,
    Function <Contract, List <String>> getterMethod
  ) {
    column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper <> (
      getterMethod.apply(cellData.getValue())
    ));

    column.setCellFactory(col -> new TableCell <Contract, List <String>> () {
      private final ComboBox <String> comboBox = new ComboBox <> ();

      @Override
      protected void updateItem(List <String> items, boolean empty) {
        super.updateItem(items, empty);

        if (empty || items.isEmpty()) {
          setGraphic(null);
        } else {
          comboBox.getItems().setAll(items);
          comboBox.setValue(items.isEmpty() ? null : items.get(0));
          comboBox.getStyleClass().add(styleClass);

          setGraphic(comboBox);
          comboBox.setEditable(isFocused());
          comboBox.focusedProperty().addListener(
            (observable, oldValue, newValue) -> {
              contractTable.getSelectionModel().select(getIndex());
              comboBox.setEditable(newValue);
            });
          blockTypingExceptCtrlC(comboBox);
        }
      }
    });
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
      newContractStage.setWidth(720);
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

  private void setupActionColumn() {
    downloadColumn.setCellFactory(param -> new TableCell<Contract, String>() {
      private final Button downloadButton = new Button();
      private final HBox actionBox = new HBox(19, downloadButton);

      {
        setupButton(
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
          }, true);
        actionBox.setAlignment(Pos.CENTER);
      }
      // Show the button
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : actionBox);
      }
    });
  }

  public void initialize() {
    try {
      if (contractService == null) {
        throw new IllegalStateException(
          "ClientService was not initialized. Call setClientService() " +
          "before loading the controller."
        );
      }
      contractTable.setPlaceholder(new Label("Não há contratos registrados"));

      setupFilterCombobox();

      setupButton(
        downloadButton, "icons/download.png",
        event -> downloadContracts(), false
      );
      setupButton(
        searchButton, "icons/search-icon.png",
        event -> searchContracts(), false
      );
      setupButton(
        addContractButton, "icons/add-icon.png",
        event -> openNewContractForm(), false
      );
      setupPagination();

      // Set up column sizes
      setupCell(contractIdColumn, 140);
      setupCell(tenantColumn, 225);
      setupCell(landlordColumn, 225);

      setupCell(rentBeginningColumn, 225);
      setupCell(rentEndColumn, 225);
      setupCell(rentValueColumn, 225);

      setupCell(guaranteeTypeColumn, 225);
      setupCell(guarantorColumn, 220);

      // Configure columns
      configureColumn();
    } catch (IllegalStateException e) {
      Alerts.showAlert(
        "Erro ao abrir página ",
        e.getMessage(), null, AlertType.ERROR
      );
    }
  }
}
