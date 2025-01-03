package gui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import db.DbException;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import model.entities.Contract;
import model.entities.Guarantee.GuaranteeType;
import services.ClientService;
import services.ContractService;
import utils.Currency;
import utils.Icons;

public class ContractListController {
  private int currentPage = 1;
  private final int pageSize = 10;
  private ClientService clientService;

  @FXML
  private TableView <Contract> contractTable;

  @FXML
  private HBox searchContainer;

  @FXML
  private TextField searchField;

  @FXML
  private Button searchButton;

  @FXML
  private ComboBox <String> filtersCombobox;

  @FXML
  private Label message;

  @FXML
  private Button addContractButton;

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
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    button.setOnAction(event);
    if (isTransparent)
      button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
  }

  private void searchContracts() {
    String filter = filtersCombobox.getSelectionModel().getSelectedItem();
    String searchText = searchField.getText().trim();

    if (filter == null || filter.isEmpty()) {
      Alerts.showAlert(
        null, "Selecione o filtro apropriado.", null, AlertType.ERROR
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
          null, "Nenhum contrato encontrado", null, AlertType.ERROR
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

  private ObservableList <Contract> getContractData(int page) {
    try {
      return FXCollections.observableArrayList(
        contractService.findPaginated(page, pageSize)
      );
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.observableArrayList();
    }
  }

  public void refreshTableData() {
    contractTable.setItems(getContractData(currentPage));
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

  private <T> void setupCell(TableColumn <Contract, T> column, double width)
  {
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
      String tenant = clientService.findClientById(contract.getTenant());
      return new SimpleStringProperty(tenant);
    });

    landlordColumn.setCellValueFactory(col -> {
      Contract contract = col.getValue();
      String landlord = clientService.findClientById(contract.getLandlord());
      return new SimpleStringProperty(landlord);
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
      new SimpleObjectProperty <> (
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
          return Double.parseDouble(string.replaceAll("[^\\d.]", ""));
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

    setupComboBoxColumn(guarantorColumn, "transparent-combobox", col -> {
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
        getClass().getResource("/gui/NewContractForm.fxml")
      );
      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == NewContractFormController.class) {
          NewContractFormController controller =
            new NewContractFormController();

          controller.setContractService(new ContractService());
          controller.setContractController(this);
          controller.setClientService(new ClientService());

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

      NewContractFormController controller = loader.getController();
      Stage newContractStage = new Stage();
      controller.setStage(newContractStage);

      newContractStage.setTitle("Adicionar contrato");
      newContractStage.setScene(new Scene(root));
      newContractStage.setWidth(640);
      newContractStage.setHeight(920);

      // Show the new contract form stage
      newContractStage.show();
      newContractStage.setResizable(false);
    } catch (IOException e) {
      e.printStackTrace();
      Alerts.showAlert(
        "Error", "Unable to load the form", e.getMessage(), AlertType.ERROR
      );
    }
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
        searchButton, "src/icons/search-icon.png",
        event -> searchContracts(), false
      );
      setupButton(
        addContractButton, "src/icons/add-icon.png",
        event -> openNewContractForm(), false
      );
      setupPagination();

      // Set up column sizes
      setupCell(contractIdColumn, 225);
      setupCell(tenantColumn, 225);
      setupCell(landlordColumn, 225);

      setupCell(rentBeginningColumn, 225);
      setupCell(rentEndColumn, 225);
      setupCell(rentValueColumn, 225);

      setupCell(guaranteeTypeColumn, 225);
      setupCell(guarantorColumn, 220);

      // Configure columns
      configureColumn();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
