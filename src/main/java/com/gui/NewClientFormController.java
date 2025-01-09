package com.gui;

import java.time.LocalDate;

import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Client.ClientType;
import com.model.entities.Client.MaritalStatus;
import com.services.ClientService;
import com.utils.CpfCnpj;
import com.utils.Date;
import com.utils.Icons;
import com.utils.States;
import com.utils.TelephoneMask;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class NewClientFormController {
  private Client client;
  private Stage stage;
  private ClientService clientService;
  private ClientListController clientListController;

  @FXML
  private ComboBox<String> typeComboBox;

  @FXML
  private RadioButton cpfRadioButton;

  @FXML
  private RadioButton cnpjRadioButton;

  @FXML
  private TextField cpfCnpjField;

  @FXML
  private TextField rgField;

  @FXML
  private TextField issuingOrganizationField;

  @FXML
  private TextField nameField;

  @FXML
  private TextField telephoneField;

  @FXML
  private DatePicker birthDateField;

  @FXML
  private Button saveButton;

  @FXML
  private Button cancelButton;

  @FXML
  private TextField nationalityField;

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

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "src/main/java/com/icons/favicon.png");
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setClientListController(ClientListController clientListController)
  {
    this.clientListController = clientListController;
  }

  @FXML
  private void initialize() {
    if (clientService == null)
      throw new IllegalStateException(
        "ClientService was not initialized. " +
        "Call setClientService() before loading the controller."
      );

    typeComboBox.getItems().addAll("Locador", "Locatário");
    typeComboBox.setValue("Locador");

    maritalStatusComboBox.getItems().addAll(
      "solteiro(a)", "casado(a)", "divorciado(a)", "viúvo(a)"
    );
    maritalStatusComboBox.setValue("solteiro(a)");

    TelephoneMask.applyTelephoneMask(telephoneField);

    cpfCnpjField.setPromptText("XXX.XXX.XXX-XX");

    ToggleGroup cpfCnpjToggleGroup = new ToggleGroup();
    cpfRadioButton.setToggleGroup(cpfCnpjToggleGroup);
    cnpjRadioButton.setToggleGroup(cpfCnpjToggleGroup);

    cpfRadioButton.setOnAction(
      event -> CpfCnpj.updateCpfCnpjMask(cpfRadioButton, cpfCnpjField)
    );
    cnpjRadioButton.setOnAction(
      event -> CpfCnpj.updateCpfCnpjMask(cnpjRadioButton, cpfCnpjField)
    );

    CpfCnpj.applyCpfCnpjMaskOnInputFields(
      cpfCnpjField, cpfRadioButton, cnpjRadioButton
    );
    
    States.populateStateCombobox(stateCombobox);

    stateCombobox.setValue("MS");

    birthDateField.setConverter(Date.getDateConverter());
    Date.applyDateMaskOnInputFields(birthDateField);

    // Restrict zipField input to digits only and limit to 8 characters
    zipField.textProperty().addListener((observable, oldValue, newValue) -> {
      // Allow only digits
      if (!newValue.matches("\\d*"))
        zipField.setText(newValue.replaceAll("[^\\d]", ""));

      // Limit input to 8 digits
      if (zipField.getText().length() > 8)
        zipField.setText(zipField.getText().substring(0, 8));
    });

    saveButton.setOnAction(event -> handleSave());
    cancelButton.setOnAction(event -> closeWindow());
  }

  private void handleSave() {
    String name = nameField.getText().replaceAll("\\d", "");
    String cpfCnpj = cpfCnpjField.getText();
    String rg = rgField.getText();
    String issuingOrganization =
      issuingOrganizationField.getText().replaceAll("\\d", "");
    String telephone = telephoneField.getText().replaceAll("\\D", "");
    LocalDate birthDate = birthDateField.getValue();
    String selectedType = typeComboBox.getValue();
    String nationality = nationalityField.getText().replaceAll("\\d", "");
    String profession = professionField.getText().replaceAll("\\d", "");
    String address = addressField.getText();
    String neighborhood = neighborhoodField.getText().replaceAll("\\d", "");
    String city = cityField.getText().replaceAll("\\d", "");
    String state = stateCombobox.getValue();
    String zip = zipField.getText();
    String invalidField = null;

    if (name == null || name.trim().isEmpty()) {
      invalidField = "Nome";
    } else if (rg == null || rg.trim().isEmpty()) {
      invalidField = "RG";
    } else if (
      issuingOrganization == null || issuingOrganization.trim().isEmpty()
    ) {
      invalidField = "Órgão expedidor";
    } else if (cpfCnpj == null || cpfCnpj.trim().isEmpty() ||
      (cpfRadioButton.isSelected() && !CpfCnpj.isCpf(cpfCnpj)) ||
      (cnpjRadioButton.isSelected() && !CpfCnpj.isCnpj(cpfCnpj))) {
      invalidField = "CPF/CNPJ";
    } else if (telephone == null || telephone.trim().isEmpty()) {
      invalidField = "Telefone";
    } else if (!Date.isValidDate(birthDate)) {
      Alerts.showAlert(
        "Data inválida", "Insira uma data válida", null, AlertType.WARNING
      );
      return;
    } else if (nationality == null || nationality.trim().isEmpty()) {
      invalidField = "Nacionalidade";
    } else if (profession == null || profession.trim().isEmpty()) {
      invalidField = "Profissão";
    } else if (address == null || address.trim().isEmpty()) {
      invalidField = "Endereço";
    } else if (neighborhood == null || neighborhood.trim().isEmpty()) {
      invalidField = "Bairro";
    } else if (city == null || city.trim().isEmpty()) {
      invalidField = "Cidade";
    } else if (state == null || state.trim().isEmpty()) {
      invalidField = "Estado";
    } else if (zip == null || zip.trim().isEmpty()) {
      invalidField = "CEP";
    }

    if (invalidField != null) {
      Alerts.showAlert(
        "Campo inválido", "Corrija o campo '" + invalidField + "'",
        null, AlertType.WARNING
      );
      return;
    }

    ClientType clientType =
      "Locador".equals(selectedType) ? ClientType.LANDLORD : ClientType.TENANT;

    // Marital status mapping using switch
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

    if (client == null) client = new Client();

    // Setting values to the client object
    client.setName(name);
    client.setCpfCnpj(cpfCnpj);
    client.setRg(rg);
    client.setIssuingOrganization(issuingOrganization);
    client.setTelephone(telephone);
    client.setBirthDate(birthDate);
    client.setClientType(clientType);
    client.setNationality(nationality);
    client.setMaritalStatus(maritalStatus);
    client.setProfession(profession);
    client.setAddress(address);
    client.setNeighborhood(neighborhood);
    client.setCity(city);
    client.setState(state);
    client.setZip(zip);

    try {
      clientService.insert(client);
      closeWindow();
    } catch (DbException e) {
      Alerts.showAlert("Erro ao salvar ", e.getMessage(), null, AlertType.ERROR);
    }
    clientListController.refreshTableData();
    clientListController.setupPagination();
  }  

  private void closeWindow() {
    stage.close();
  }
}
