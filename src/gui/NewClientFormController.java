package gui;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
  private ComboBox <String> typeComboBox;

  @FXML
  private VBox cpfCnpjChoices;

  @FXML
  private RadioButton cpfRadioButton;

  @FXML
  private RadioButton cnpjRadioButton;

  @FXML
  private TextField cpfCnpjField;

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
        "ClientService was not initialized. Call setClientService() " +
        "before loading the controller."
      );
    }

    // Populate ComboBox with options
    typeComboBox.getItems().addAll("Locador", "Locatário");
    typeComboBox.setValue("Locador"); // Default selection

    // Add telephone mask listener
    TelephoneMask.applyTelephoneMask(telephoneField);

    // Set initial CPF or CNPJ mask for 'Pessoa Física'
    cpfCnpjField.setPromptText("XXX.XXX.XXX-XX");

    // Create a ToggleGroup for CPF and CNPJ radio buttons to ensure only one can be selected
    ToggleGroup cpfCnpjToggleGroup = new ToggleGroup();
    cpfRadioButton.setToggleGroup(cpfCnpjToggleGroup);
    cnpjRadioButton.setToggleGroup(cpfCnpjToggleGroup);

    // Add action listeners to radio buttons
    cpfRadioButton.setOnAction(event -> CpfCnpj.updateCpfCnpjMask(cpfRadioButton, cpfCnpjField));
    cnpjRadioButton.setOnAction(event -> CpfCnpj.updateCpfCnpjMask(cnpjRadioButton, cpfCnpjField));

    CpfCnpj.applyCpfCnpjMask(cpfCnpjField, cpfRadioButton, cnpjRadioButton);

    saveButton.setOnAction(event -> handleSave());
    cancelButton.setOnAction(event -> closeWindow());
  }

  // Handle save action
  private void handleSave() {
    String name = nameField.getText();
    // Extract only digits from cpfCnpj and telephone fields
    String cpfCnpj = cpfCnpjField.getText().replaceAll("\\D", "");
    String telephone = telephoneField.getText().replaceAll("\\D", "");
    LocalDate birthDate = birthDateField.getValue();
    String selectedType = typeComboBox.getValue();

    if (client == null) client = new Client();

    // Validate inputs and save
    if (
      name != null && !name.trim().isEmpty() &&
      telephone != null && !telephone.trim().isEmpty() &&
      birthDate != null &&
      cpfCnpj != null && !cpfCnpj.trim().isEmpty()
    ) {
    	// Validate CPF or CNPJ
        if (!CpfCnpj.isValid(cpfCnpj)) {
          return; // If CPF/CNPJ is invalid, stop further processing
        }
      // Map the selected type (Locador -> LANDLORD, Locatário -> TENANT)
      ClientType clientType =
        "Locador".equals(selectedType) ?
        ClientType.LANDLORD : ClientType.TENANT;

      client.setName(name);
      client.setCpfCnpj(cpfCnpj);
      client.setTelephone(telephone);
      // Birthdate will already be in the correct format (yyyy-MM-dd)
      client.setBirthDate(birthDate);
      client.setClientType(clientType);
      client.setGuarantee(null);
      client.setGuarantorName(null);

      // Call the save callback (service layer)
      clientService.insert(client);
      clientListController.refreshTableData();

      closeWindow();
      clientListController.setupPagination();
    }
  }

  // Close window after saving
  private void closeWindow() {
    stage.close();
  }
}