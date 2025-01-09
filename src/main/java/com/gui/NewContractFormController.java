package com.gui;

import java.time.LocalDate;
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
import com.utils.Date;
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
  private DatePicker contractSigningDateField;

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

    rentBeginningField.setConverter(Date.getDateConverter());
    rentEndField.setConverter(Date.getDateConverter());
    contractSigningDateField.setConverter(Date.getDateConverter());

    Date.applyDateMaskOnInputFields(rentBeginningField);
    Date.applyDateMaskOnInputFields(rentEndField);
    Date.applyDateMaskOnInputFields(contractSigningDateField);
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
  
  private void applyDoubleFormatter(TextField textField) {
    textField.setText("R$ ");
    textField.setTextFormatter(Currency.allowOnlyDigitsAndSeparators());
    // Set the caret position to the end of the initial text
    textField.positionCaret(textField.getText().length());
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

  private void populateContractData(
    Contract contract, Integer tenantId, Integer landlordId, Integer estateId,
    LocalDate rentBeginning, LocalDate rentEnd, LocalDate contractSigningDate
  ) {
    contract.setTenant(tenantId);
    contract.setLandlord(landlordId);
    contract.setEstate(estateId);
    contract.setRentBeginning(rentBeginning);
    contract.setRentEnd(rentEnd);
    contract.setRentValue(Currency.parseMonetaryValue(rentValue.getText()));
    contract.setEnergyBill(Currency.parseMonetaryValue(energyBill.getText()));
    contract.setWaterBill(Currency.parseMonetaryValue(waterBill.getText()));
    contract.setDepositValue(Currency.parseMonetaryValue(deposit.getText()));
    contract.setContractSigningDate(contractSigningDate);
  }

  private String validateInputs(
    Integer tenantId, Integer landlordId, Integer estateId,
    LocalDate rentBeginning, LocalDate rentEnd, LocalDate contractSigningDate,
    String guaranteeType, TextField deposit, TextField guarantorName
  ) {
    // Validate tenant, landlord, and estate
    if (tenantField.getValue() == null || tenantId == null)
      return "Locatário";
    if (landlordField.getValue() == null || landlordId == null)
      return "Locador";
    if (estateField.getValue() == null || estateId == null)
      return "Imóvel";

    // Validate monetary fields
    if (rentValue.getText().replace("R$", "").trim().isEmpty())
      return "Valor do aluguel";
    if (energyBill.getText().replace("R$", "").trim().isEmpty())
      return "Valor da conta de energia";
    if (waterBill.getText().replace("R$", "").trim().isEmpty())
      return "Valor da conta de água";

    // Validate dates
    if (!Date.isValidDate(rentBeginning))
      return "Insira uma data válida para o início do contrato";
    if (!Date.isValidDate(rentEnd))
      return "Insira uma data válida para o término do contrato";
    if (!Date.isValidDate(contractSigningDate))
      return "Insira uma data válida para a assinatura";
    if (!rentEnd.isAfter(rentBeginning))
      return "Data de término deve ser posterior à data de início";

    // Validate guarantee
    if (guaranteeType == null || guaranteeType.trim().isEmpty())
      return "Selecione um tipo de garantia";

    if (guaranteeType.equals("Caução")) {
      String depositText = deposit.getText().replace("R$", "").trim();
      if (depositText.isEmpty() || Double.parseDouble(depositText) == 0.0)
        return "Valor da caução não pode ser R$ 0";
    }

    if (guaranteeType.equals("Fiador")) {
      if (guarantorName == null || guarantorName.getText().trim().isEmpty())
        return "Nome do fiador não pode ser vazio";
    }

    return null;
  }

  private void closeWindow() {
    stage.close();
  }

  private void handleSave() {
    if (contract == null) contract = new Contract();
    if (guarantee == null) guarantee = new Guarantee();

    Integer tenantId = tenantMap.get(tenantField.getValue());
    Integer landlordId = landlordMap.get(landlordField.getValue());
    Integer estateId = estateMap.get(estateField.getValue());
    LocalDate rentBeginning = rentBeginningField.getValue();
    LocalDate rentEnd = rentEndField.getValue();
    LocalDate contractSigningDate = contractSigningDateField.getValue();

    String validationError = validateInputs(
      tenantId, landlordId, estateId, rentBeginning,
      rentEnd, contractSigningDate, guaranteeType.getValue(),
      deposit, guarantorName
    );

    if (validationError != null) {
      Alerts.showAlert("Erro de Validação", validationError, null, AlertType.WARNING);
      return;
    }

    populateContractData(
      contract, tenantId, landlordId, estateId,
      rentBeginning, rentEnd, contractSigningDate
    );

    String selectedGuaranteeType = guaranteeType.getValue();
    Guarantee.GuaranteeType guaranteeTypeEnum =
      guaranteeTypeMap.get(selectedGuaranteeType);
    guarantee.setGuaranteeType(guaranteeTypeEnum);
    guarantee.setGuarantorNames(
      List.of(guarantorName.getText(), partnerName.getText())
    );
    contract.setGuarantee(guarantee);

    try {
      contractService.insert(contract);
    } catch (DbException e) {
      Alerts.showAlert("Erro ao salvar", e.getMessage(), null, AlertType.ERROR);
      return;
    }

    contractController.refreshTableData();
    contractController.setupPagination();
    closeWindow();
  }
}
