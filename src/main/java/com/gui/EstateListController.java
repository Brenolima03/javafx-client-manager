package com.gui;

import java.io.IOException;
import java.util.List;

import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Estate;
import com.services.ClientService;
import com.services.EstateService;
import com.utils.ActionColumn;
import com.utils.CustomContextMenu;
import com.utils.Icons;
import com.utils.TableCellConfiguration;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.stage.Stage;

public class EstateListController {
  private int currentPage = 1;
  private final int pageSize = 10;
  private EstateService estateService;
  private ClientService clientService;

  @FXML
  private TableView<Estate> estateTable;

  @FXML
  private TableColumn <Estate, String> updateDeleteColumn;

  @FXML
  private TextField searchField;

  @FXML
  private Button searchButton;

  @FXML
  private ComboBox <String> filterCombobox;

  @FXML
  private Label message;

  @FXML
  private Button addEstateButton;

  @FXML
  private TableColumn<Estate, String> addressColumn;

  @FXML
  private TableColumn<Estate, String> numberColumn;

  @FXML
  private TableColumn<Estate, String> neighborhoodColumn;

  @FXML
  private TableColumn<Estate, String> cityColumn;

  @FXML
  private TableColumn<Estate, String> stateColumn;

  @FXML
  private TableColumn<Estate, String> tenantColumn;

  @FXML
  private TableColumn<Estate, String> landlordColumn;

  @FXML
  private TableColumn<Estate, String> descriptionColumn;

  @FXML
  private Pagination pagination;

  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  private void setupFilterCombobox() {
    filterCombobox.setItems(
      FXCollections.observableArrayList(
        "Endereço", "Bairro", "Locador", "Valor do aluguel"
      )
    );
    filterCombobox.setValue("Endereço");
  }

  private void handleUpdate(Estate estate) {
    System.out.println("Opening the update form.");
  }

  public void refreshTableData(ObservableList<Estate> estateList) {
    estateTable.getItems().clear();
    estateTable.setItems(estateList);
    ActionColumn actionColumn = new ActionColumn();

    actionColumn.setupActionColumn(
      (TableColumn<Estate, String>) estateTable.getColumns().get(8),
      event -> {
        // Get the cell containing the button, then get the row from the cell
        TableCell<Estate, String> cell =
          (TableCell<Estate, String>) ((Button) event.getSource())
            .getParent().getParent();

        Estate estate = cell.getTableRow().getItem();
        estateTable.getSelectionModel().select(estate);
        handleUpdate(estate);
      },
      event -> {},
      false
    );
  }

  public void setupPagination(ObservableList<Estate> clientList) {
    int totalEstates = clientList.size();
    int pageCount = (totalEstates + pageSize - 1) / pageSize;

    pagination.setPageCount(pageCount);
    pagination.setVisible(pageCount > 1);
    pagination.setCurrentPageIndex(currentPage - 1);
    pagination.setMaxPageIndicatorCount(5);

    // Set up the page factory to update the table based on selected page
    pagination.setPageFactory(pageIndex -> {
      currentPage = pageIndex + 1;

      // Calculate the sublist for the current page
      int fromIndex = (currentPage - 1) * pageSize;
      int toIndex = Math.min(fromIndex + pageSize, totalEstates);
      List<Estate> estatesForPage = clientList.subList(fromIndex, toIndex);

      refreshTableData(FXCollections.observableArrayList(estatesForPage));
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
      List<Estate> estates = argument.isEmpty() 
        ? estateService.findAllEstates() 
        : estateService.search(filter, argument);

      if (estates == null || estates.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum imóvel encontrado", null, AlertType.ERROR
        );
        estates = estateService.findAllEstates();
      }

      setupPagination(FXCollections.observableArrayList(estates));
      searchField.clear();
    } catch (DbException e) {
      Alerts.showAlert("Erro ao buscar", e.getMessage(), null, AlertType.ERROR);
    }
  }

  private void openNewEstateForm() {
    try {
      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/com/gui/NewEstateForm.fxml")
      );
      loader.setControllerFactory(controllerClass -> {
        if (controllerClass == NewEstateFormController.class) {
          NewEstateFormController controller =
            new NewEstateFormController();
          controller.setEstateService(new EstateService());
          controller.setClientService(new ClientService());
          controller.setEstateService(new EstateService());
          controller.setEstateController(this);
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

      NewEstateFormController controller = loader.getController();
      Stage newEstateStage = new Stage();
      controller.setStage(newEstateStage);

      newEstateStage.setTitle("Adicionar imóvel");
      newEstateStage.setScene(new Scene(root));
      newEstateStage.setWidth(640);
      newEstateStage.setHeight(580);
      newEstateStage.show();
      newEstateStage.setResizable(false);
    } catch (IOException e) {
      Alerts.showAlert(
        "Error", "Unable to load the form",
        e.getMessage(), AlertType.ERROR
      );
    }
  }

  private void populateCells(
    CustomContextMenu contextMenu, TableCellConfiguration tableCellConfig
  ) {
    tableCellConfig.configureCellFactory(
      addressColumn, 225, "address", contextMenu
    );

    tableCellConfig.configureCellFactory(
      numberColumn, 225, "number", contextMenu
    );

    tableCellConfig.configureCellFactory(
      neighborhoodColumn, 225, "neighborhood", contextMenu
    );

    tableCellConfig.configureCellFactory(
      cityColumn, 225, "city", contextMenu
    );

    tableCellConfig.configureCellFactory(
      stateColumn, 140, "state", contextMenu
    );

    tableCellConfig.configureCellFactory(
      tenantColumn, 225, "tenantId", contextMenu
    );

    tableCellConfig.configureCellFactory(
      landlordColumn, 225, "landlordId", contextMenu
    );

    tableCellConfig.configureCellFactory(
      descriptionColumn, 220, "description", contextMenu
    );

    tenantColumn.setCellValueFactory(cellData -> {
      Integer tenantId = cellData.getValue().getTenantId();
      if (tenantId == null || tenantId == 0)
        return new SimpleStringProperty("");

      Client tenantObj = clientService.findClientById(tenantId);
      return new SimpleStringProperty(
        tenantObj != null ? tenantObj.getName() : ""
      );
    });

    landlordColumn.setCellValueFactory(cellData -> {
      Integer landlordId = cellData.getValue().getLandlordId();
      if (landlordId == null || landlordId == 0)
        return new SimpleStringProperty("");

      Client landlordObj = clientService.findClientById(landlordId);
      return new SimpleStringProperty(
        landlordObj != null ? landlordObj.getName() : ""
      );
    });
  }

  public void initialize() {
    try {
      if (estateService == null)
        throw new IllegalStateException(
          "EstateService was not initialized. " +
          "Call setEstateService() before loading the controller."
        );

      Icons.setupTransparentButton(
        searchButton, "icons/search-icon.png",
        event -> handleSearch()
      );
      Icons.setupTransparentButton(
        addEstateButton, "icons/add-icon.png",
        event -> openNewEstateForm()
      );

      estateTable.setPlaceholder(new Label("Não há imóveis cadastrados"));

      CustomContextMenu contextMenu = new CustomContextMenu();
      TableCellConfiguration tableCellConfig = new TableCellConfiguration();

      searchField.focusedProperty().addListener(
      (observable, oldValue, newValue) -> {
        if (newValue)
          contextMenu.setCustomContextMenuForTextFields(searchField);
      });
      contextMenu.setCustomContextMenuForTextFields(searchField);

      setupFilterCombobox();
      setupPagination(
        FXCollections.observableArrayList(estateService.findAllEstates())
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
