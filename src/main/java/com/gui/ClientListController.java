package com.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.application.Main;
import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Client.ClientType;
import com.services.ClientService;
import com.utils.ActionColumn;
import com.utils.CpfCnpj;
import com.utils.CustomContextMenu;
import com.utils.Icons;
import com.utils.TableCellConfiguration;
import com.utils.TelephoneMask;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientListController extends Main {
  private int currentPage = 1;
  private final int pageSize = 10;
  private ClientService clientService;

  @FXML
  private TableView <Client> clientTable;

  @FXML
  private TableColumn <Client, String> nameColumn;

  @FXML
  private TableColumn <Client, String> cpfCnpjColumn;

  @FXML
  private TableColumn <Client, String> rgColumn;

  @FXML
  private TableColumn <Client, String> telephoneColumn;

  @FXML
  private TableColumn <Client, String> maritalStatusColumn;

  @FXML
  private TableColumn <Client, String> professionColumn;

  @FXML
  private TableColumn <Client, String> addressColumn;

  @FXML
  private TableColumn <Client, String> neighborhoodColumn;

  @FXML
  private TableColumn <Client, String> cityColumn;

  @FXML
  private TableColumn <Client, String> stateColumn;

  @FXML
  private TableColumn <Client, String> clientTypeColumn;

  @FXML
  private TableColumn <Client, List<String>> contractsColumn;

  @FXML
  private TableColumn <Client, String> updateDeleteColumn;

  @FXML
  private TextField searchField;

  @FXML
  private ComboBox <String> filterCombobox;

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

  private void handleUpdate(Client client) {
    System.out.println("Opening the update form.");
  }

  private void handleDelete(Client client) {
    if (Alerts.showConfirmation("Confirma?", "Remover cliente?")
      .filter(result -> result == ButtonType.OK).isPresent()
    ) {
      try {
        boolean isLandlord = client.getClientType().equals(ClientType.LANDLORD);

        clientService.delete(client.getId(), isLandlord);

        setupPagination(
          FXCollections.observableArrayList(clientService.findAllClients())
        );
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

  private void setupFilterCombobox() {
    filterCombobox.setItems(FXCollections.observableArrayList(
      "Nome",
      "CPF | CNPJ",
      "RG",
      "Telefone",
      "Contrato",
      "Tipo"
    ));
    filterCombobox.setValue("Nome");
  }

  public void refreshTableData(ObservableList<Client> clientList) {
    clientTable.getItems().clear();
    clientTable.setItems(clientList);
    ActionColumn actionColumn = new ActionColumn();

    actionColumn.setupActionColumn(
      (TableColumn<Client, String>) clientTable.getColumns().get(12),
      event -> {
        // Get the cell containing the button, then get the row from the cell
        TableCell<Client, String> cell =
          (TableCell<Client, String>) ((Button) event.getSource())
            .getParent().getParent();

        Client client = cell.getTableRow().getItem();
        clientTable.getSelectionModel().select(client);
        handleUpdate(client);
      },
      event -> {
        // Get the cell containing the button, then get the row from the cell
        TableCell<Client, String> cell =
          (TableCell<Client, String>) ((Button) event.getSource())
            .getParent().getParent();

        Client client = cell.getTableRow().getItem();
        clientTable.getSelectionModel().select(client);
        handleDelete(client);
      },
      true
    );
  }

  public void setupPagination(ObservableList<Client> clientList) {
    int totalClients = clientList.size();
    int pageCount = (totalClients + pageSize - 1) / pageSize;

    pagination.setPageCount(pageCount);
    pagination.setVisible(pageCount > 1);
    pagination.setCurrentPageIndex(currentPage - 1);
    pagination.setMaxPageIndicatorCount(5);

    // Set up the page factory to update the table based on selected page
    pagination.setPageFactory(pageIndex -> {
      currentPage = pageIndex + 1;

      // Calculate the sublist for the current page
      int fromIndex = (currentPage - 1) * pageSize;
      int toIndex = Math.min(fromIndex + pageSize, totalClients);
      List<Client> clientsForPage = clientList.subList(fromIndex, toIndex);

      refreshTableData(FXCollections.observableArrayList(clientsForPage));
      return new Label("");
    });
  }

  private void handleSearch() {
    String filter = filterCombobox.getSelectionModel().getSelectedItem();
    String argument = searchField.getText().trim().replaceAll("[()\\-./]", "");

    if (filter == null || filter.isEmpty()) {
      Alerts.showAlert(
        null, "Selecione o filtro apropriado.", null, AlertType.ERROR)
      ;
      return;
    }

    try {
      List<Client> clients = argument.isEmpty() 
        ? clientService.findAllClients() 
        : clientService.search(filter, argument);

      if (clients == null || clients.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum cliente encontrado", null, AlertType.ERROR
        );
        clients = clientService.findAllClients();
      }

      setupPagination(FXCollections.observableArrayList(clients));
      searchField.clear();
    } catch (DbException e) {
      Alerts.showAlert("Erro ao buscar", e.getMessage(), null, AlertType.ERROR);
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
        } catch (Exception e) {
          System.err.println(e.getMessage());
        }
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

  private void populateCells(
    CustomContextMenu contextMenu, TableCellConfiguration tableCellConfig
  ) {
    tableCellConfig.configureCellFactory(
      nameColumn, 200, "name", contextMenu
    );

    tableCellConfig.configureCellFactory(
      cpfCnpjColumn, 180, "cpfCnpj", contextMenu
    );

    tableCellConfig.configureCellFactory(
      rgColumn, 120, "rg", contextMenu
    );

    tableCellConfig.configureCellFactory(
      telephoneColumn, 150, "telephone", contextMenu
    );

    tableCellConfig.configureCellFactory(
      maritalStatusColumn, 150, "maritalStatus", contextMenu
    );

    tableCellConfig.configureCellFactory(
      professionColumn, 150, "profession", contextMenu
    );

    tableCellConfig.configureCellFactory(
      addressColumn, 170, "address", contextMenu
    );

    tableCellConfig.configureCellFactory(
      neighborhoodColumn, 150, "neighborhood", contextMenu
    );

    tableCellConfig.configureCellFactory(
      cityColumn, 140, "city", contextMenu
    );

    tableCellConfig.configureCellFactory(
      stateColumn, 100, "state", contextMenu
    );

    tableCellConfig.configureCellFactory(
      clientTypeColumn, 100, "clientType", contextMenu
    );

    tableCellConfig.configureCellFactory(
      contractsColumn, 100, "contracts", contextMenu
    );

    cpfCnpjColumn.setCellValueFactory(cellData -> {
      String cpfCnpj = cellData.getValue().getCpfCnpj();
      String formattedCpfCnpj = CpfCnpj.applyCpfCnpjMaskFromDatabase(cpfCnpj);
      return new SimpleStringProperty(formattedCpfCnpj);
    });

    telephoneColumn.setCellValueFactory(cellData -> {
      String telephone = cellData.getValue().getTelephone();
      String formattedTelephone =
        TelephoneMask.applyTelephoneMaskFromDatabase(telephone);
      return new SimpleStringProperty(formattedTelephone);
    });

    TableCellConfiguration.setupComboBoxColumn(
      clientTable, contractsColumn, "transparent-combobox", col -> {
      List<Integer> contracts = col.getContracts();
      List<String> contractsStr = new ArrayList<>();
      for (Integer contract : contracts)
        contractsStr.add(contract.toString());
      return contractsStr != null && !contractsStr.isEmpty() ?
        contractsStr : Collections.emptyList();
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
        addClientButton, "icons/add-icon.png",
        event -> openNewClientForm()
      );

      clientTable.setPlaceholder(new Label("Não há clientes registrados"));

      CustomContextMenu contextMenu = new CustomContextMenu();
      TableCellConfiguration tableCellConfig = new TableCellConfiguration();

      searchField.focusedProperty().addListener(
      (observable, oldValue, newValue) -> {
        if (newValue)
          contextMenu.setCustomContextMenuForTextFields(searchField);
      });

      setupFilterCombobox();
      setupPagination(
        FXCollections.observableArrayList(clientService.findAllClients())
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
