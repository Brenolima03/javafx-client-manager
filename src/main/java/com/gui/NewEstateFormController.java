package com.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Client.ClientType;
import com.model.entities.Estate;
import com.services.ClientService;
import com.services.EstateService;
import com.utils.CpfCnpj;
import com.utils.Icons;
import com.utils.States;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewEstateFormController {
  private Estate estate;
  private Stage stage;
  private EstateService estateService;
  private ClientService clientService;
  private EstateListController estateController;

  @FXML
  private ComboBox<String> landlordField;

  @FXML
  private TextField addressField;

  @FXML
  private TextField numberField;

  @FXML
  private TextField neighborhoodField;

  @FXML
  private TextField cityField;

  @FXML
  private ComboBox<String> stateComboBox;

  @FXML
  private TextField descriptionField;

  @FXML
  private Button saveButton;

  @FXML
  private Button cancelButton;

  private final Map<String, Integer> landlordMap = new HashMap<>();

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "icons/favicon.png");
  }

  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setEstateController(EstateListController controller) {
    this.estateController = controller;
  }

  @FXML
  private void initialize() {
    if (estateService == null)
      throw new IllegalStateException(
        "EstateService was not initialized. " +
        "Call setEstateService() before loading the controller.");

    if (estateController != null) estateController.refreshTableData();
    else System.out.println("estateController is not initialized!");

    setupButtons();
    populateTenantAndLandlordFields();
    States.populateStateCombobox(stateComboBox);
    stateComboBox.setValue("MS");
  }

  private void setupButtons() {
    saveButton.setOnAction(event -> handleSave());
    cancelButton.setOnAction(event -> closeWindow());
  }

  private void populateTenantAndLandlordFields() {
    List<String> landlords = getClientNamesAndCpfOrCnpj(
      clientService.findAllClients(), Client.ClientType.LANDLORD
    );
    landlordField.getItems().addAll(landlords);
    landlordField.setPromptText("Locador");
  }

  private List<String> getClientNamesAndCpfOrCnpj(
    List<Client> clients, ClientType type
  ) {
    List<String> filteredClients = new ArrayList<>();
    for (Client client : clients) {
      if (client.getClientType() == type) {
        String displayText = client.getName() + " - " +
          CpfCnpj.applyCpfCnpjMaskFromDatabase(client.getCpfCnpj());
        filteredClients.add(displayText);
        if (type == ClientType.LANDLORD)
          landlordMap.put(displayText, client.getId());
      }
    }
    return filteredClients;
  }

  private void handleSave() {
    if (estate == null) estate = new Estate();
  
    // Retrieve values from the form fields
    String address = addressField.getText();
    String number = numberField.getText();
    String neighborhood = neighborhoodField.getText();
    String city = cityField.getText();
    String state = stateComboBox.getValue();
    String landlord = landlordField.getValue();
    String description = descriptionField.getText();
    String invalidField = null;

    // Validate each field and identify the invalid one
    if (address == null || address.trim().isEmpty()) {
      invalidField = "Endereço";
    } else if (number == null || number.trim().isEmpty()) {
      invalidField = "Número";
    } else if (neighborhood == null || neighborhood.trim().isEmpty()) {
      invalidField = "Bairro";
    } else if (city == null || city.trim().isEmpty()) {
      invalidField = "Cidade";
    } else if (landlord == null || landlord.trim().isEmpty()) {
      invalidField = "Locador";
    } else if (description == null || description.trim().isEmpty()) {
      invalidField = "Descrição";
    }

    // If an invalid field is detected, show an alert and return
    if (invalidField != null) {
      Alerts.showAlert(
        "Erro de Validação",
        "Campo inválido: " + invalidField,
        null,
        AlertType.WARNING
      );
      return;
    }

    // Set the values into the Estate object
    estate.setAddress(address);
    estate.setNumber(Integer.parseInt(number));
    estate.setNeighborhood(neighborhood);
    estate.setCity(city);
    estate.setState(state);
    estate.setLandlordId(landlordMap.get(landlord));
    estate.setDescription(description);

    // Save estate to the database
    try {
      estateService.insert(estate);
    } catch (DbException e) {
      Alerts.showAlert(
        "Erro ao salvar",
        e.getMessage(),
        null,
        AlertType.ERROR
      );
      return;
    }

    // Refresh the data and close the window
    estateController.refreshTableData();
    estateController.setupPagination();
    closeWindow();
  }

  private void closeWindow() {
    stage.close();
  }
}
