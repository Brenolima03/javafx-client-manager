package gui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import application.Main;
import db.DbException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import model.entities.Client;
import services.ClientService;
import utils.Currency;
import utils.Icons;

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
  private TableColumn <Client, List <String>> contractColumn;

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
  private HBox searchContainer;

  @FXML
  private TextField searchField;

  @FXML
  private HBox pages;

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

  public ClientListController() {
    this.clientService = new ClientService();
  }

  private void blockTypingExceptCtrlC(Node node) {
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
      blockTypingExceptCtrlC(cell);
      return cell;
    });
  }


  private void configureColumn() {
    // Configuring editable columns
    configureEditableColumn(nameColumn, "name",
      (client, newValue) ->
      setCellValue(client, "name", newValue)
    );
    configureEditableColumn(telephoneColumn, "telephone",
      (client, newValue) ->
      setCellValue(client, "telephone", newValue)
    );

    // Configuring non-editable columns
    configureNonEditableColumn(cpfCnpjColumn, "cpfCnpj");
    configureNonEditableColumn(birthDateColumn, "birthDate");
    configureNonEditableColumn(contractColumn, "contracts");
    configureNonEditableColumn(guaranteeTypeColumn, "guarantee");
    configureNonEditableColumn(guarantorColumn, "guarantors");
    configureNonEditableColumn(depositColumn, "deposit");
    configureNonEditableColumn(typeColumn, "clientType");

    setupComboBoxColumn(contractColumn, "transparent-combobox",
      client -> getContractsByClientCpfCnpj(client.getCpfCnpj())
    );

    setupComboBoxColumn(guarantorColumn, "transparent-combobox",
      client -> getGuarantors(client.getCpfCnpj())
    );

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
      blockTypingExceptCtrlC(cell);
      return cell;
    });

    depositColumn.setCellFactory(col -> {
      TextFieldTableCell<Client, Double> cell =
      new TextFieldTableCell<>(Currency.getCurrencyConverter());

      blockTypingExceptCtrlC(cell);
      return cell;
    });
  }

  private List <String> getContractsByClientCpfCnpj(String cpfCnpj) {
    return clientService.getContractsByClientCpfCnpj(cpfCnpj);
  }
  private List <String> getGuarantors(String cpfCnpj) {
    return clientService.getGuarantorsByClientCpfCnpj(cpfCnpj);
  }

  private void setupFilterCombobox() {
    filtersCombobox.setItems(FXCollections.observableArrayList(
      "Nome",
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
          blockTypingExceptCtrlC(comboBox);
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
        setupButton(
          editButton, "src/icons/update-icon.png",
          event -> handleEdit(event), true
        );

        setupButton(
          deleteButton, "src/icons/delete-icon.png",
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
      e.printStackTrace();
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
      case "name":
        client.setName((String) value);
        break;
      case "telephone":
        client.setTelephone((String) value);
        break;
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
    } catch (Exception e) {
      e.printStackTrace();
    }
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
        null, "Selecione o filtro apropriado.", null, AlertType.ERROR
      );
      return;
    }

    try {
      List <Client> clients = searchText.isEmpty() ?
        clientService.findPaginated(currentPage = 1, pageSize) :
        clientService.search(filter, searchText);

      if (clients == null || clients.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum cliente encontrado", null, AlertType.ERROR
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
      Alerts.showConfirmation("Tem certeza?", "Atualizar este cliente?");

      if (result.isPresent() && result.get() == ButtonType.OK) {
        try {
          clientService.update(client);

          if (
            alteredClient != null &&
            alteredClient.equals(client)
          ) {
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
            "Erro ao atualizar o cliente", null, e.getMessage(), AlertType.ERROR
          );
        }
      }
    }
  }

  private void removeEntity(Client client) {
    if (Alerts.showConfirmation("Tem certeza?", "Remover este cliente?")
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
          "Erro ao remover cliente", null, e.getMessage(), AlertType.ERROR
        );
      }
    }
  }

  private void openNewClientForm() {
    try {
      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/gui/NewClientForm.fxml")
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
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      });
      Parent root = loader.load();

      NewClientFormController controller = loader.getController();

      Stage newClientStage = new Stage();
      controller.setStage(newClientStage);

      newClientStage.setTitle("Adicionar cliente");
      newClientStage.setScene(new Scene(root));
      newClientStage.setWidth(640);
      newClientStage.setHeight(980);

      newClientStage.show();

    } catch (IOException e) {
      e.printStackTrace();
      Alerts.showAlert(
        "Error", "Unable to load the form", e.getMessage(), AlertType.ERROR
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
        searchButton, "src/icons/search-icon.png",
        event -> searchClients(), false
      );
      setupButton(
        addClientButton, "src/icons/add-icon.png",
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
