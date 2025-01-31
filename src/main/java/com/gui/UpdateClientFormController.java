package com.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.db.DbException;
import com.model.entities.Client.MaritalStatus;
import com.services.ClientService;
import com.utils.CustomContextMenu;
import com.utils.Icons;
import com.utils.States;
import com.utils.TelephoneMask;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateClientFormController {
  private int clientId;
  private Stage stage;
  private ClientService clientService;
  private ClientListController clientListController;

  @FXML
  private TextField telephoneField;

  @FXML
  private ComboBox<String> maritalStatusComboBox;

  @FXML
  private TextField professionField;

  @FXML
  private TextField addressField;

  @FXML
  private TextField neighborhoodField;

  @FXML
  private TextField cityField;

  @FXML
  private ComboBox<String> stateCombobox;

  @FXML
  private TextField zipField;

  @FXML
  private Button saveButton;

  @FXML
  private Button cancelButton;

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "icons/favicon.png");
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setClientListController(ClientListController clientListController)
  {
    this.clientListController = clientListController;
  }

  public void setClientId(int clientId) {
    this.clientId = clientId;
  }

  private void closeWindow() {
    stage.close();
  }

  private void handleSave() {
    Map<String, String> fieldMap = Map.of(
      "Telefone", telephoneField.getText().replaceAll("\\D", "").trim(),
      "Profissão", professionField.getText().replaceAll("\\d", "").trim(),
      "Endereço", addressField.getText().trim(),
      "Bairro", neighborhoodField.getText().replaceAll("\\d", "").trim(),
      "Cidade", cityField.getText().replaceAll("\\d", "").trim(),
      "Estado", stateCombobox.getValue(),
      "CEP", zipField.getText().trim()
    );

    // Convert marital status
    String selectedMaritalStatus = maritalStatusComboBox.getValue();
    MaritalStatus maritalStatus = switch (selectedMaritalStatus) {
      case "solteiro(a)" -> MaritalStatus.SINGLE;
      case "casado(a)" -> MaritalStatus.MARRIED;
      case "divorciado(a)" -> MaritalStatus.DIVORCED;
      case "viúvo(a)" -> MaritalStatus.WIDOWED;
      default -> throw new IllegalArgumentException(
        "Estado civil desconhecido: " + selectedMaritalStatus
      );
    };

    // Create a map for the updated fields
    LinkedHashMap<String, Object> updatedFields = new LinkedHashMap<>();
    updatedFields.put("telephone", fieldMap.get("Telefone"));
    updatedFields.put("maritalStatus", maritalStatus.name());
    updatedFields.put("profession", fieldMap.get("Profissão"));
    updatedFields.put("address", fieldMap.get("Endereço"));
    updatedFields.put("neighborhood", fieldMap.get("Bairro"));
    updatedFields.put("city", fieldMap.get("Cidade"));
    updatedFields.put("state", fieldMap.get("Estado"));
    updatedFields.put("zip", fieldMap.get("CEP"));

    updatedFields
      .values().removeIf(value -> value == null || value.toString().isEmpty());

    try {
      clientService.update(clientId, updatedFields);

      closeWindow();
      clientListController.setupPagination(
        FXCollections.observableArrayList(clientService.findAllClients())
      );
    } catch (DbException e) {
      Alerts.showAlert("Erro ao salvar", e.getMessage(), null, AlertType.ERROR);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void initialize() {
    if (clientService == null)
      throw new IllegalStateException(
        "ClientService was not initialized. " +
        "Call setClientService() before loading the controller."
      );

    TelephoneMask.applyTelephoneMask(telephoneField);

    maritalStatusComboBox.getItems().addAll(
      "solteiro(a)", "casado(a)", "divorciado(a)", "viúvo(a)"
    );
    maritalStatusComboBox.setValue("solteiro(a)");
        stateCombobox.setItems(
      FXCollections.observableArrayList(States.getAllStates())
    );
    stateCombobox.setValue("MS");
    zipField.textProperty().addListener((observable, oldValue, newValue) -> {
      // Allow only digits
      if (!newValue.matches("\\d*"))
        zipField.setText(newValue.replaceAll("[^\\d]", ""));

      // Limit input to 8 digits
      if (zipField.getText().length() > 8)
        zipField.setText(zipField.getText().substring(0, 8));
    });

    CustomContextMenu contextMenu = new CustomContextMenu();

    telephoneField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(telephoneField);
    });
    professionField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(professionField);
    });
    addressField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(addressField);
    });
    neighborhoodField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(neighborhoodField);
    });
    cityField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(cityField);
    });
    zipField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(zipField);
    });

    saveButton.setOnAction(event -> handleSave());
    cancelButton.setOnAction(event -> closeWindow());
  }
}
