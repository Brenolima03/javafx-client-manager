package com.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.application.Main;
import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Client.ClientType;
import com.model.entities.Guarantee.GuaranteeType;
import com.services.ClientService;
import com.utils.CpfCnpj;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ClientListController extends Main {
  private int currentPage = 1;
  private final int pageSize = 10;
  private Client alteredClient = null;
  private Client originalClientValues = null;
  private ClientService clientService;

  @FXML
  private TableView <Client> clientTable;

  @FXML
  private TableColumn <Client, String> nameColumn;

  @FXML
  private TableColumn <Client, String> cpfCnpjColumn;

  @FXML
  private TableColumn <Client, LocalDate> birthDateColumn;

  @FXML
  private TableColumn <Client, String> contractColumn;

  @FXML
  private TableColumn <Client, String> telephoneColumn;

  @FXML
  private TableColumn <Client, String> guaranteeTypeColumn;

  @FXML
  private TableColumn <Client, List <String>> guarantorColumn;

  @FXML
  private TableColumn <Client, Double> depositColumn;

  @FXML
  private TableColumn <Client, String> typeColumn;

  @FXML
  private TableColumn <Client, String> updateDeleteColumn;

  @FXML
  private TextField searchField;

  @FXML
  private ComboBox <String> filtersCombobox;

  @FXML
  private Label message;

  @FXML
  private Button searchButton;

  @FXML
  private Button addClientButton;

  @FXML
  private Pagination pagination;

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  private void blockKeyboardExceptCtrlC(Node node) {
    node.addEventFilter(KeyEvent.ANY, event -> {
      if (!(event.isControlDown() && event.getCode() == KeyCode.C))
        event.consume();
    });
  }
  
  public void refreshTableData() {
    clientTable.setItems(getClientData(currentPage));
    setupActionColumn();
  }

  private <T> void setupCell(TableColumn<Client, T> column, double width) {
    column.setPrefWidth(width);
    column.setCellFactory(param -> new TableCell<>() {
      private final TextField textField = new TextField();
      {
        textField.setStyle(
          "-fx-background-color: transparent; -fx-alignment: center-left;"
        );
      }
    });
  }

  // Configures an editable column with a PropertyValueFactory and edit handling
  private void configureEditableColumn(
    TableColumn<Client, String> column, String property,
    BiConsumer<Client, String> editHandler
  ) {
    clientTable.setEditable(true);
    column.setEditable(true);
    column.setCellValueFactory(new PropertyValueFactory<>(property));
    column.setCellFactory(TextFieldTableCell.forTableColumn());
    column.setOnEditCommit(event -> {
      Client rowValue = event.getRowValue();
      String newValue = event.getNewValue();
      editHandler.accept(rowValue, newValue);
    });
  }

  // Configures a non-editable column for any data type
  private <T> void configureNonEditableColumn(
    TableColumn<Client, T> column,
    String property
  ) {
    column.setCellValueFactory(new PropertyValueFactory<>(property));

    column.setCellFactory(tc -> {
      TextFieldTableCell<Client, T> cell =
      new TextFieldTableCell<>(new StringConverter<T>() {
        @Override
        public String toString(T object) {
          return object == null ? "" : object.toString();
        }

        @Override
        public T fromString(String string) {
          return null;
        }
      });
      blockKeyboardExceptCtrlC(cell);
      return cell;
    });
  }

  private void configureColumn() {
    // Configuring editable columns
    configureEditableColumn(nameColumn, "name", (client, newValue) ->
      setCellValue(client, "name", newValue)
    );
    configureEditableColumn(telephoneColumn, "telephone", (client, newValue) ->
      setCellValue(client, "telephone", newValue)
    );

    // Configuring non-editable columns
    configureNonEditableColumn(cpfCnpjColumn, "cpfCnpj");
    configureNonEditableColumn(birthDateColumn, "birthDate");
    configureNonEditableColumn(contractColumn, "contract");
    configureNonEditableColumn(guarantorColumn, "guarantors");
    configureNonEditableColumn(typeColumn, "clientType");

    cpfCnpjColumn.setCellValueFactory(cellData -> {
      String cpfCnpj = cellData.getValue().getCpfCnpj();
      String formattedCpfCnpj = CpfCnpj.applyCpfCnpjMaskFromDatabase(cpfCnpj);
      return new SimpleStringProperty(formattedCpfCnpj);
    });

    contractColumn.setCellValueFactory(cellData -> {
      Integer contract = cellData.getValue().getContract();
      String contractValue = (contract == 0 ? null : contract.toString());
      return new SimpleStringProperty(contractValue);
    });

    setupComboBoxColumn(guarantorColumn, "transparent-combobox", col -> {
      if (col.getClientType().equals(ClientType.TENANT)) {
        int contract = col.getContract();
        List<String> guarantors = clientService.getGuarantorsById(contract);
        return guarantors != null && !guarantors.isEmpty() ?
          guarantors : Collections.emptyList();
      } else return Collections.emptyList();
    });

    birthDateColumn.setCellFactory(col -> {
      TextFieldTableCell<Client, LocalDate> cell =
      new TextFieldTableCell<>(new StringConverter<LocalDate>() {
        private final DateTimeFormatter dateFormatter =
          DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public String toString(LocalDate object) {
          return object == null ? "" : object.format(dateFormatter);
        }

        @Override
        public LocalDate fromString(String string) {
          return null;  // Prevent changes to the date from the user input
        }
      });
      blockKeyboardExceptCtrlC(cell);
      return cell;
    });
    guaranteeTypeColumn.setCellValueFactory(cellData -> {
      String guaranteeType = "";
      int contract = cellData.getValue().getContract();

      if (cellData.getValue().getClientType().equals(ClientType.TENANT))
        guaranteeType = clientService.getGuaranteeTypeByContractId(contract);

      return new SimpleStringProperty(guaranteeType);
    });

    guaranteeTypeColumn.setCellFactory(tc -> {
      TextFieldTableCell<Client, String> cell =
        new TextFieldTableCell<>(new StringConverter<String>() {
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
          return string; // No conversion needed
        }
      });
      blockKeyboardExceptCtrlC(cell);
      return cell;
    });

    depositColumn.setCellFactory(tc -> {
      // Create a TableCell that will display the deposit value as editable
      TableCell<Client, Double> cell = new TextFieldTableCell<Client, Double>(){
        @Override
        public void updateItem(Double item, boolean empty) {
          super.updateItem(item, empty);
          if (empty || item == null) setText(null);
          else setText(Currency.getCurrencyConverter().toString(item));
        }
      };

    depositColumn.setCellValueFactory(cellData -> {
      int contract = cellData.getValue().getContract();
      Double deposit =
        cellData.getValue().getClientType().equals(ClientType.TENANT) &&
        clientService.getDeposit(contract) != 0 ?
        clientService.getDeposit(contract) : null;
        return new SimpleObjectProperty<>(deposit);
      });
      blockKeyboardExceptCtrlC(cell);
      return cell;
    });
  }

  private void setupFilterCombobox() {
    filtersCombobox.setItems(FXCollections.observableArrayList(
      "Contrato",
      "CPF | CNPJ",
      "Telefone",
      "Contrato",
      "Tipo"
    ));
    filtersCombobox.setValue("Nome");
  }

  private void setupComboBoxColumn(
    TableColumn <Client, List <String>> column, String styleClass,
    Function <Client, List <String>> getterMethod
  ) {
    column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper <> (
      getterMethod.apply(cellData.getValue())
    ));

    column.setCellFactory(col -> new TableCell <Client, List <String>> () {
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
            clientTable.getSelectionModel().select(getIndex());
            comboBox.setEditable(newValue);
          });
          blockKeyboardExceptCtrlC(comboBox);
        }
      }
    });
  }

  private void setupActionColumn() {
    updateDeleteColumn.setCellFactory(param -> new TableCell <Client, String> ()
    {
      private final Button editButton = new Button();
      private final Button deleteButton = new Button();
      private final HBox actionBox = new HBox(19, editButton, deleteButton);
      {
        setupButton(editButton, "src/main/java/com/icons/update-icon.png",
          event -> handleEdit(event), true
        );

        setupButton(deleteButton, "src/main/java/com/icons/delete-icon.png",
          event -> handleDelete(event), true
        );

        actionBox.setAlignment(Pos.CENTER);
      }

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : actionBox);
      }

      private void handleEdit(ActionEvent event) {
        Client client = getTableView().getItems().get(getIndex());
        updateClient(client);
      }

      private void handleDelete(ActionEvent event) {
        Client client = getTableView().getItems().get(getIndex());
        removeEntity(client);
      }
    });
  }

  private ObservableList <Client> getClientData(int page) {
    try {
      return FXCollections.observableArrayList(
        clientService.findPaginated(page, pageSize)
      );
    } catch (Exception e) {
      return FXCollections.observableArrayList();
    }
  }

  private void setCellValue(Client client, String field, Object value) {
    try {
      if (originalClientValues == null) {
        originalClientValues = new Client();
        originalClientValues.setId(client.getId());
        originalClientValues.setName(client.getName());
        originalClientValues.setTelephone(client.getTelephone());
      }

      switch (field) {
      case "name" -> client.setName((String) value);
      case "telephone" -> client.setTelephone((String) value);
      }

      boolean valueChanged =
        !client.getName().equals(originalClientValues.getName()) ||
        !client.getTelephone().equals(originalClientValues.getTelephone());

      // Handle UI updates based on whether changes exist
      if (valueChanged) {
        int row = clientTable.getItems().indexOf(client);
        clientTable.getItems().set(row, client);
        clientTable.refresh();
        alteredClient = client;
        lockOrUnlockCells(client, true);

        message.setText(
          "Há atualizações a ser salvas para " + client.getName() +
          ". Clique no ícone à esquerda e depois em OK."
        );
      } else {
        message.setText("");
        alteredClient = null;
        clientTable.refresh();
        lockOrUnlockCells(client, false);
        originalClientValues = null;
        clientTable.getSelectionModel().clearSelection();
      }
    } catch (Exception e) {}
  }

  private void setupButton(
    Button button, String path, EventHandler <ActionEvent> event,
    boolean isTransparent
  ) {
    Icons.setButtonIcon(button, path);
    button.setOnAction(event);
    if (isTransparent)
      button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
  }

  public void setupPagination() {
    int totalClients = clientService.count();
    int pageCount = (totalClients + pageSize - 1) / pageSize;

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

  private void searchClients() {
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
      List <Client> clients = searchText.isEmpty() ?
        clientService.findPaginated(currentPage = 1, pageSize) :
        clientService.search(filter, searchText);

      if (clients == null || clients.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum cliente encontrado",
          null, AlertType.ERROR
        );
        return;
      }

      int totalClients = searchText.isEmpty() ?
        clientService.count() : clients.size();
      int pageCount = (totalClients + pageSize - 1) / pageSize;

      pagination.setPageCount(pageCount);
      pagination.setVisible(pageCount > 1);
      pagination.setCurrentPageIndex(Math.min(currentPage, pageCount) - 1);

      clientTable.getSortOrder().clear();
      Platform.runLater(() -> {
        clientTable.getItems().setAll(clients);
      });

    } catch (DbException e) {
      Alerts.showAlert(
        "Erro ao buscar ", e.getMessage(), null, AlertType.ERROR
      );
    }
  }

  private void lockOrUnlockCells(Client client, boolean disable) {
    clientTable.setRowFactory(tv -> new TableRow <Client> () {
      @Override
      protected void updateItem(Client item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
          // Disable all rows except the one being edited
          if (disable && !item.equals(client)) {
            setDisable(true);
            getStyleClass().add("locked-row");
          } else {
            setDisable(false);
            getStyleClass().remove("locked-row");
          }
        }
      }
    });
  }

  private void updateClient(Client client) {
    if (alteredClient != null && client.equals(alteredClient)) {
      Optional <ButtonType> result =
        Alerts.showConfirmation("Confirma?", "Atualizar cliente?");

      if (result.isPresent() && result.get() == ButtonType.OK) {
        try {
          clientService.update(client);

          if (alteredClient != null && alteredClient.equals(client)) {
            message.setText("");
            alteredClient = null;
            originalClientValues = null;
          }

          Alerts.showAlert(
            "Sucesso", "Cliente atualizado com sucesso!",
            null, AlertType.INFORMATION
          );

          lockOrUnlockCells(client, false);
          clientTable.refresh();
        } catch (DbException e) {
          Alerts.showAlert(
            "Erro ao atualizar o cliente", null,
            e.getMessage(), AlertType.ERROR
          );
        }
      }
    }
  }

  private void removeEntity(Client client) {
    if (Alerts.showConfirmation("Confirma?", "Remover cliente?")
      .filter(result -> result == ButtonType.OK).isPresent()
    ) {
      try {
        clientService.delete(client.getId());
        setupPagination();
        Alerts.showAlert(
          "Sucesso", "Cliente apagado com sucesso!",
          null, AlertType.INFORMATION
        );

      } catch (DbException e) {
        Alerts.showAlert(
          "Erro ao remover cliente!", null,
          e.getMessage(), AlertType.ERROR
        );
      }
    }
  }

  private void openNewClientForm() {
    try {
      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/com/gui/NewClientForm.fxml")
      );
      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == NewClientFormController.class) {
          NewClientFormController controller = new NewClientFormController();
          controller.setClientService(new ClientService());
          controller.setClientListController(this);
          return controller;
        }
        try {
          return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {}
        return null;
      });
      Parent root = loader.load();

      NewClientFormController controller = loader.getController();

      Stage newClientStage = new Stage();
      controller.setStage(newClientStage);

      newClientStage.setTitle("Adicionar cliente");
      newClientStage.setScene(new Scene(root));
      newClientStage.setWidth(720);
      newClientStage.setHeight(980);

      newClientStage.show();
      newClientStage.setResizable(false);
    } catch (IOException e) {
      Alerts.showAlert(
        "Erro", "Não foi possível abrir o formulário!",
        e.getMessage(), AlertType.ERROR
      );
    }
  }

  public void initialize() {
    try {
      if (clientService == null) {
        throw new IllegalStateException(
          "ClientService was not initialized. Call setClientService() " +
          "before loading the controller."
        );
      }
      clientTable.setPlaceholder(new Label("Não há clientes registrados"));
      setupFilterCombobox();
      setupButton(
        searchButton, "src/main/java/com/icons/search-icon.png",
        event -> searchClients(), false
      );
      setupButton(
        addClientButton, "src/main/java/com/icons/add-icon.png",
        event -> openNewClientForm(), false
      );
      setupPagination();

      setupCell(nameColumn, 300);
      setupCell(cpfCnpjColumn, 210);
      setupCell(birthDateColumn, 150);
      setupCell(telephoneColumn, 200);
      setupCell(contractColumn, 170);
      setupCell(guaranteeTypeColumn, 200);
      setupCell(guarantorColumn, 220);
      setupCell(depositColumn, 130);
      setupCell(typeColumn, 130);

      configureColumn();
    } catch (IllegalStateException e) {
      Alerts.showAlert(
        "Erro ao abrir página ",
        e.getMessage(), null, AlertType.ERROR
      );
    }
  }
}
