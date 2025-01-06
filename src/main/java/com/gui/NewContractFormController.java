package com.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.db.DbException;
import com.model.entities.Client;
import com.model.entities.Client.ClientType;
import com.model.entities.Contract;
import com.model.entities.Estate;
import com.model.entities.Guarantee;
import com.services.ClientService;
import com.services.ContractService;
import com.services.EstateService;
import com.utils.CpfCnpj;
import com.utils.Currency;
import com.utils.Icons;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewContractFormController {
  private Contract contract;
  private Guarantee guarantee;
  private Stage stage;
  private ContractService contractService;
  private ClientService clientService;
  private EstateService estateService;
  private ContractListController contractController;

  @FXML
  private ComboBox<String> tenantField;

  @FXML
  private ComboBox<String> landlordField;

  @FXML
  private ComboBox<String> estateField;

  @FXML
  private ComboBox<String> guaranteeType;

  @FXML
  private TextField guarantorName;

  @FXML
  private TextField partnerName;

  @FXML
  private DatePicker rentBeginningField;

  @FXML
  private DatePicker rentEndField;

  @FXML
  private TextField rentValue;

  @FXML
  private TextField energyBill;

  @FXML
  private TextField waterBill;

  @FXML
  private TextField deposit;

  @FXML
  private DatePicker contractSigningDate;

  @FXML
  private Button saveButton;

  @FXML
  private Button cancelButton;

  private final Map<String, Guarantee.GuaranteeType> guaranteeTypeMap = 
    new HashMap<>();

  private final Map<String, Integer> tenantMap = new HashMap<>();
  private final Map<String, Integer> landlordMap = new HashMap<>();
  private final Map<String, Integer> estateMap = new HashMap<>();

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "src/main/java/com/icons/favicon.png");
  }

  public void setContractService(ContractService contractService) {
    this.contractService = contractService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  public void setContractController(ContractListController controller) {
    this.contractController = controller;
  }

  @FXML
  private void initialize() {
    if (contractService == null)
      throw new IllegalStateException(
        "ContractService was not initialized. " +
        "Call setContractService() before loading the controller."
      );

    if (contractController != null) contractController.refreshTableData();

    initializeGuaranteeTypeComboBox();
    setupButtons();
    populateEstateCombobox();
    populateTenantAndLandlordFields();

    // Apply double formatter to all relevant fields
    applyDoubleFormatter(rentValue);
    applyDoubleFormatter(energyBill);
    applyDoubleFormatter(waterBill);
    applyDoubleFormatter(deposit);
  }

  private void applyDoubleFormatter(TextField textField) {
    textField.setText("R$ ");
    textField.setTextFormatter(Currency.allowOnlyDigitsAndSeparators());
    // Set the caret position to the end of the initial text
    textField.positionCaret(textField.getText().length());
  }

  private void initializeGuaranteeTypeComboBox() {
    guaranteeTypeMap.put("Caução", Guarantee.GuaranteeType.DEPOSIT);
    guaranteeTypeMap.put("Fiador", Guarantee.GuaranteeType.GUARANTOR);
    guaranteeTypeMap.put("Capitalização", 
      Guarantee.GuaranteeType.CAPITALIZATION_TITLE);
    guaranteeTypeMap.put("Seguro fiança", 
      Guarantee.GuaranteeType.BAIL_INSURANCE);

    ObservableList<String> guaranteeTypes = FXCollections.observableArrayList(
      guaranteeTypeMap.keySet()
    );
    guaranteeType.setItems(guaranteeTypes);
    guaranteeType.setPromptText("Garantia");
  }

  private void setupButtons() {
    saveButton.setOnAction(event -> handleSave());
    cancelButton.setOnAction(event -> closeWindow());
  }

  private void populateTenantAndLandlordFields() {
    List<String> tenants = getClientNamesAndCpfOrCnpj(
      clientService.findAllClients(), Client.ClientType.TENANT
    );
    tenantField.getItems().addAll(tenants);
    tenantField.setPromptText("Locatário");

    List<String> landlords = getClientNamesAndCpfOrCnpj(
      clientService.findAllClients(), Client.ClientType.LANDLORD
    );
    landlordField.getItems().addAll(landlords);
    landlordField.setPromptText("Locador");
  }

  private void populateEstateCombobox() {
    List<String> estates = getEstates(estateService.findAllEstates());
    estateField.getItems().addAll(estates);
    estateField.setPromptText("Imóvel");
  }
  
  private List<String> getEstates(List<Estate> estates) {
    List<String> filteredEstates = new ArrayList<>();
    for (Estate estate : estates) {
      String displayText = estate.getAddress();
      filteredEstates.add(displayText);
      estateMap.put(displayText, estate.getId());
    }
    return filteredEstates;
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
        if (type == ClientType.TENANT)
          tenantMap.put(displayText, client.getId());
        else if (type == ClientType.LANDLORD)
          landlordMap.put(displayText, client.getId());
      }
    }
    return filteredClients;
  }

  private void handleSave() {
    if (contract == null) contract = new Contract();
    if (guarantee == null) guarantee = new Guarantee();

    Integer tenantId = tenantMap.get(tenantField.getValue());
    Integer landlordId = landlordMap.get(landlordField.getValue());
    Integer estateId = estateMap.get(estateField.getValue());

    if (tenantId == null || landlordId == null || estateId == null) return;

    contract.setTenant(tenantId);
    contract.setLandlord(landlordId);
    contract.setEstate(estateId);
    contract.setRentBeginning(rentBeginningField.getValue());
    contract.setRentEnd(rentEndField.getValue());

    // Clean and parse the values for the monetary fields
    contract.setRentValue(Currency.parseMonetaryValue(rentValue.getText()));
    contract.setEnergyBill(Currency.parseMonetaryValue(energyBill.getText()));
    contract.setWaterBill(Currency.parseMonetaryValue(waterBill.getText()));
    contract.setDepositValue(Currency.parseMonetaryValue(deposit.getText()));

    contract.setContractSigningDate(contractSigningDate.getValue());

    String selectedGuaranteeType = guaranteeType.getValue();
    Guarantee.GuaranteeType guaranteeTypeEnum = 
      guaranteeTypeMap.get(selectedGuaranteeType);

    if (guaranteeTypeEnum == null) return;

    guarantee.setGuaranteeType(guaranteeTypeEnum);
    guarantee.setGuarantorNames(
      List.of(guarantorName.getText(), partnerName.getText()));
    contract.setGuarantee(guarantee);

    try {
      contractService.insert(contract);
    } catch (DbException e) {
      Alerts.showAlert(
      "Erro ao salvar ", e.getMessage(), null, AlertType.ERROR
      );
    }

    contractController.refreshTableData();
    contractController.setupPagination();
    closeWindow();
  }

  private void closeWindow() {
    stage.close();
  }
}
