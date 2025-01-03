package gui;

import java.time.LocalDate;
import java.util.List;

import db.DbException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.entities.Client;
import model.entities.Client.ClientType;
import services.ClientService;
import utils.CpfCnpj;
import utils.Icons;
import utils.TelephoneMask;

public class NewClientFormController {
  private Client client;
  private Stage stage;
  private ClientService clientService;
  private ClientListController clientListController;

  @FXML
  private VBox clientForm;

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
  private RadioButton marriedRadioButton;

  @FXML
  private RadioButton singleRadioButton;

  @FXML
  private TextField professionField;

  @FXML
  private TextField addressField;

  @FXML
  private TextField neighborhoodField;

  @FXML
  private TextField cityField;

  @FXML
  private ComboBox<String> stateField;

  @FXML
  private TextField zipField;

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "src/icons/favicon.png");
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
    if (clientService == null) {
      throw new IllegalStateException(
        "ClientService was not initialized. " +
        "Call setClientService() before loading the controller."
      );
    }

    // Populate ComboBox with options
    typeComboBox.getItems().addAll("Locador", "Locatário");
    typeComboBox.setValue("Locador"); // Default selection

    // Add telephone mask listener
    TelephoneMask.applyTelephoneMask(telephoneField);

    // Set initial CPF or CNPJ mask for 'Pessoa Física'
    cpfCnpjField.setPromptText("XXX.XXX.XXX-XX");

    // Create ToggleGroups for exclusive selection
    ToggleGroup cpfCnpjToggleGroup = new ToggleGroup();
    cpfRadioButton.setToggleGroup(cpfCnpjToggleGroup);
    cnpjRadioButton.setToggleGroup(cpfCnpjToggleGroup);

    ToggleGroup maritalStatusToggleGroup = new ToggleGroup();
    marriedRadioButton.setToggleGroup(maritalStatusToggleGroup);
    singleRadioButton.setToggleGroup(maritalStatusToggleGroup);

    // Add action listeners to radio buttons
    cpfRadioButton.setOnAction(
      event -> CpfCnpj.updateCpfCnpjMask(cpfRadioButton, cpfCnpjField)
    );
    cnpjRadioButton.setOnAction(
      event -> CpfCnpj.updateCpfCnpjMask(cnpjRadioButton, cpfCnpjField)
    );

    CpfCnpj.applyCpfCnpjMaskOnInputFields(
      cpfCnpjField, cpfRadioButton, cnpjRadioButton
    );

    // Populate state ComboBox
    List<String> states = clientService.findStates();
    ObservableList<String> observableStates =
    FXCollections.observableArrayList(states);

    stateField.setItems(observableStates);
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
    String name = nameField.getText();
    String cpfCnpj = cpfCnpjField.getText().replaceAll("\\D", "");
    String rg = rgField.getText().replaceAll("\\D", "");
    String issuingOrganization = issuingOrganizationField.getText();
    String telephone = telephoneField.getText().replaceAll("\\D", "");
    LocalDate birthDate = birthDateField.getValue();
    String selectedType = typeComboBox.getValue();
    String nationality = nationalityField.getText();
    boolean isMarried = marriedRadioButton.isSelected();

    String profession = professionField.getText();
    String address = addressField.getText();
    String neighborhood = neighborhoodField.getText();
    String city = cityField.getText();
    String state = stateField.getValue();
    String zip = zipField.getText();

    if (
      name == null || name.trim().isEmpty() ||
      cpfCnpj == null || cpfCnpj.trim().isEmpty() ||
      telephone == null || telephone.trim().isEmpty() ||
      birthDate == null) {
      return;
    }

    ClientType clientType =
      "Locador".equals(selectedType) ? ClientType.LANDLORD : ClientType.TENANT;

    if (client == null) client = new Client();
    if (!CpfCnpj.isValid(cpfCnpj)) return;

    client.setName(name);
    client.setCpfCnpj(cpfCnpj);
    client.setRg(rg);
    client.setIssuingOrganization(issuingOrganization);
    client.setTelephone(telephone);
    client.setBirthDate(birthDate);
    client.setClientType(clientType);
    client.setNationality(nationality);
    client.setIsMarried(isMarried);
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
      e.printStackTrace();
    }
    clientListController.refreshTableData();
    clientListController.setupPagination();
  }

  private void closeWindow() {
    stage.close();
  }
}
