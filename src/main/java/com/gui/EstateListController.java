package com.gui;

import java.io.IOException;
import java.util.List;

import com.db.DbException;
import com.model.entities.Estate;
import com.services.ClientService;
import com.services.EstateService;
import com.utils.Icons;

import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class EstateListController {
  private int currentPage = 1;
  private final int pageSize = 10;
  private EstateService estateService;
  private ClientService clientService;

  @FXML
  private TableView<Estate> estateTable;

  @FXML
  private TextField searchField;

  @FXML
  private Button searchButton;

  @FXML
  private ComboBox <String> filtersCombobox;

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

  private Stage currentStage;

  public void setStage(Stage stage) {
    this.currentStage = stage;
  }

  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  private void setupFilterCombobox() {
    filtersCombobox.setItems(
      FXCollections.observableArrayList(
        "Endereço", "Bairro", "Locador", "Valor do aluguel"
      )
    );
    filtersCombobox.setValue("Endereço");
  }

  @FXML
  public void openEstateList(ActionEvent event) {
    if (currentStage == null) {
      System.err.println(
        "Error: Current stage is not set. Ensure setStage() is called."
      );
      return;
    }

    try {
      VBox root = new VBox();
      root.getChildren().add(new Label("Estate List View"));

      if (pagination != null) {
        root.getChildren().add(pagination);
      }

      Scene scene = new Scene(root);
      currentStage.setScene(scene);
      currentStage.show();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  private ObservableList <Estate> getEstateData(int page) {
    try {
      return FXCollections.observableArrayList(
        estateService.findPaginated(page, pageSize)
      );
    } catch (Exception e) {
      return FXCollections.observableArrayList();
    }
  }

  public void refreshTableData() {
    estateTable.setItems(getEstateData(currentPage));
  }

  public void setupPagination() {
    int totalEstates = estateService.count();
    int pageCount = (totalEstates + pageSize - 1) / pageSize;

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

  private <T> void setupCell(TableColumn <Estate, T> column, double width) {
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

  private <T> void configureNonEditableColumn(
    TableColumn <Estate, T> column,
    String property
  ) {
    column.setCellValueFactory(new PropertyValueFactory <> (property));
    column.setCellFactory(tc -> {
      TextFieldTableCell <Estate, T > cell =
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
    configureNonEditableColumn(addressColumn, "address");
    configureNonEditableColumn(numberColumn, "number");
    configureNonEditableColumn(neighborhoodColumn, "neighborhood");
    configureNonEditableColumn(cityColumn, "city");
    configureNonEditableColumn(stateColumn, "state");
    configureNonEditableColumn(tenantColumn, "tenantId");
    configureNonEditableColumn(landlordColumn, "landlordId");
    configureNonEditableColumn(descriptionColumn, "description");
  
    tenantColumn.setCellValueFactory(col -> {
      int tenantId = col.getValue().getTenantId();
      String tenant = clientService.findClientById(tenantId);
      return new SimpleStringProperty(tenant);
    });
  
    landlordColumn.setCellValueFactory(col -> {
      int landlordId = col.getValue().getLandlordId();
      String landlord = clientService.findClientById(landlordId);
      return new SimpleStringProperty(landlord);
    });
  }
  

  private void searchEstates() {
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
      List <Estate> estates =
        searchText.isEmpty() ?
        estateService.findPaginated(currentPage = 1, pageSize) :
        estateService.search(filter, searchText);

      if (estates == null || estates.isEmpty()) {
        Alerts.showAlert(
          null, "Nenhum imóvel encontrado",
          null, AlertType.ERROR
        );
        return;
      }

      int totalEstates = searchText.isEmpty() ?
        estateService.count() : estates.size();
      int pageCount = (totalEstates + pageSize - 1) / pageSize;

      pagination.setPageCount(pageCount);
      pagination.setVisible(pageCount > 1);
      pagination.setCurrentPageIndex(Math.min(currentPage, pageCount) - 1);

      estateTable.getSortOrder().clear();
      Platform.runLater(() -> {
        estateTable.getItems().setAll(estates);
      });

    } catch (DbException e) {
      Alerts.showAlert(
        "Erro ao buscar ", e.getMessage(), null, AlertType.ERROR
      );
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
      newEstateStage.setHeight(920);

      // Show the new Estate form stage
      newEstateStage.show();
      newEstateStage.setResizable(false);
    } catch (IOException e) {
      Alerts.showAlert(
        "Error", "Unable to load the form",
        e.getMessage(), AlertType.ERROR
      );
    }
  }

  public void initialize() {
    try {
      if (estateService == null)
        throw new IllegalStateException(
          "EstateService was not initialized. " +
          "Call setEstateService() before loading the controller."
        );

      estateTable.setPlaceholder(new Label("Não há imóveis cadastrados"));

      setupFilterCombobox();

      setupButton(
        searchButton, "src/main/java/com/icons/search-icon.png",
        event -> searchEstates(), false
      );
      setupButton(
        addEstateButton, "src/main/java/com/icons/add-icon.png",
        event -> openNewEstateForm(), false
      );
      setupPagination();
      setupCell(addressColumn, 225);
      setupCell(numberColumn, 225);
      setupCell(neighborhoodColumn, 225);

      setupCell(cityColumn, 225);
      setupCell(stateColumn, 225);
      setupCell(tenantColumn, 225);

      setupCell(landlordColumn, 225);
      setupCell(descriptionColumn, 220);

      configureColumn();
    } catch (IllegalStateException e) {
      Alerts.showAlert(
        "Erro ao abrir página ",
        e.getMessage(), null, AlertType.ERROR
      );
    }
  }
}
