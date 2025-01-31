package com.gui;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.utils.CustomContextMenu;
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
  private TextField rentValueField;
  
  @FXML
  private TextField depositField;

  @FXML
  private TextField energyConsumerUnit;

  @FXML
  private TextField waterRegistrationNumber;

  @FXML
  private DatePicker rentBeginningField;

  @FXML
  private DatePicker rentEndField;

  @FXML
  private DatePicker contractSigningDateField;
  
  @FXML
  private ComboBox<String> guaranteeType;

  @FXML
  private TextField guarantorName;

  @FXML
  private TextField partnerNameField;

  @FXML
  private Button saveButton;

  @FXML
  private Button cancelButton;

  private final Map<String, Guarantee.GuaranteeType> guaranteeTypeMap = 
    new HashMap<>();

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "icons/favicon.png");
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

  private void populateComboboxes() {
    Map<String, Integer> tenants = clientService.findAllClients().stream()
      .filter(client -> client.getClientType().equals(ClientType.TENANT))
      .sorted(Comparator.comparing(Client::getId))
      .collect(Collectors.toMap(
        client -> client.getName() + " - " +
          CpfCnpj.applyCpfCnpjMaskFromDatabase(client.getCpfCnpj()),
        Client::getId,
        (existing, replacement) -> existing,
        LinkedHashMap::new
      ));

    tenantField.getItems().addAll(tenants.keySet());
    tenantField.setPromptText("Locatário");
    tenantField.setOnAction(e -> {
      String selectedTenant = tenantField.getSelectionModel().getSelectedItem();

      if (selectedTenant != null) {
        Integer tenantId = tenants.get(selectedTenant);
        tenantField.setValue(selectedTenant);
        tenantField.getProperties().put("tenantId", tenantId);
      }
    });

    Map<String, Integer> landlords = clientService.findAllClients().stream()
      .filter(client -> client.getClientType().equals(ClientType.LANDLORD))
      .sorted(Comparator.comparing(Client::getId))
      .collect(Collectors.toMap(
        client -> client.getName() + " - " +
          CpfCnpj.applyCpfCnpjMaskFromDatabase(client.getCpfCnpj()),
        Client::getId,
        (existing, replacement) -> existing,
        LinkedHashMap::new
      ));

    landlordField.getItems().addAll(landlords.keySet());
    landlordField.setPromptText("Locatário");
    landlordField.setOnAction(e -> {
      Integer landlordId =
        landlords.get(landlordField.getSelectionModel().getSelectedItem());
      landlordField.getProperties().put("landlordId", landlordId);

      Map<String, Integer> estates = estateService
        .getAllClientEstates(landlordId).stream()
        .collect(Collectors.toMap(Estate::getAddress, Estate::getId));

      estateField.getItems().setAll(estates.keySet());
      estateField.setOnAction(ev -> {
        String selectedEstate =
          estateField.getSelectionModel().getSelectedItem();
        estateField.getProperties()
          .put("estateId", estates.get(selectedEstate));
      });

      if (!estates.isEmpty()) {
        String firstEstate = estates.keySet().iterator().next();
        estateField.setValue(firstEstate);
        estateField.getProperties().put("estateId", estates.get(firstEstate));
      }
    });

    estateField.setPromptText("Imóveis");
  }

  private void applyDoubleFormatter(TextField textField) {
    textField.setText("R$ ");
    textField.setTextFormatter(Currency.allowOnlyDigitsAndSeparators());
    // Set the caret position to the end of the initial text
    textField.positionCaret(textField.getText().length());
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
    contract.setRentValue(
      Currency.parseMonetaryValue(rentValueField.getText())
    );
    contract.setEnergyConsumerUnit(energyConsumerUnit.getText());
    contract.setWaterRegistrationNumber(waterRegistrationNumber.getText());
    contract.setDepositValue(
      Currency.parseMonetaryValue(depositField.getText())
    );
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
    if (rentValueField.getText().replace("R$", "").trim().isEmpty())
      return "Valor do aluguel";
    if (energyConsumerUnit.getText().trim().isEmpty())
      return "UC";
    if (waterRegistrationNumber.getText().trim().isEmpty())
      return "Matrícula Águas";

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

    Integer tenantId = (Integer) tenantField.getProperties().get("tenantId");
    Integer landlordId =
      (Integer) landlordField.getProperties().get("landlordId");
    Integer estateId = (Integer) estateField.getProperties().get("estateId");
    LocalDate rentBeginning = rentBeginningField.getValue();
    LocalDate rentEnd = rentEndField.getValue();
    LocalDate contractSigningDate = contractSigningDateField.getValue();

    String validationError = validateInputs(
      tenantId, landlordId, estateId, rentBeginning,
      rentEnd, contractSigningDate, guaranteeType.getValue(),
      depositField, guarantorName
    );

    if (validationError != null) {
      Alerts.showAlert(
        "Erro de Validação", validationError, null, AlertType.WARNING
      );
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
      List.of(guarantorName.getText(), partnerNameField.getText())
    );
    contract.setGuarantee(guarantee);

    try {
      contractService.insert(contract);
    } catch (DbException e) {
      Alerts.showAlert("Erro ao salvar", e.getMessage(), null, AlertType.ERROR);
      return;
    }

    contractController.setupPagination(
      FXCollections.observableArrayList(contractService.getAllContracts())
    );
    closeWindow();
  }

  @FXML
  private void initialize() {
    if (contractService == null)
      throw new IllegalStateException(
        "ContractService was not initialized. " +
        "Call setContractService() before loading the controller."
      );

    // Apply double formatter to all relevant fields
    applyDoubleFormatter(rentValueField);
    applyDoubleFormatter(depositField);

    Date.applyDateMaskOnInputFields(rentBeginningField);
    Date.applyDateMaskOnInputFields(rentEndField);
    Date.applyDateMaskOnInputFields(contractSigningDateField);

    CustomContextMenu contextMenu = new CustomContextMenu();

    rentValueField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(rentValueField);
    });
    depositField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(depositField);
    });
    energyConsumerUnit.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(energyConsumerUnit);
    });
    waterRegistrationNumber.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(waterRegistrationNumber);
    });
    rentBeginningField.getEditor().focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu
          .setCustomContextMenuForTextFields(rentBeginningField.getEditor());
    });
    rentEndField.getEditor().focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(rentEndField.getEditor());
    });
    contractSigningDateField.getEditor().focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(
          contractSigningDateField.getEditor()
        );
    });
    guarantorName.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(guarantorName);
    });
    partnerNameField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(partnerNameField);
    });

    initializeGuaranteeTypeComboBox();
    setupButtons();
    populateComboboxes();
  }
}
