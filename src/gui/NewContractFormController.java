package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import db.DbException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.entities.Client;
import model.entities.Client.ClientType;
import model.entities.Contract;
import model.entities.Guarantee;
import services.ClientService;
import services.ContractService;
import utils.CpfCnpj;
import utils.Currency;
import utils.Icons;

public class NewContractFormController {
  private Contract contract;
  private Guarantee guarantee;
  private Stage stage;
  private ContractService contractService;
  private ClientService clientService;
  private ContractListController contractController;

  @FXML
  private VBox contractForm;

  @FXML
  private ComboBox<String> tenantField;

  @FXML
  private ComboBox<String> landlordField;

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

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "src/icons/favicon.png");
  }

  public void setContractService(ContractService contractService) {
    this.contractService = contractService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setContractController(ContractListController controller) {
    this.contractController = controller;
  }

  @FXML
  private void initialize() {
    if (contractService == null) {
      throw new IllegalStateException(
        "ContractService was not initialized. " +
        "Call setContractService() before loading the controller.");
    }

    if (contractController != null) contractController.refreshTableData();
    else System.out.println("contractController is not initialized!");

    initializeGuaranteeTypeComboBox();
    setupButtons();
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

    if (tenantId == null || landlordId == null) return;

    contract.setTenant(tenantId);
    contract.setLandlord(landlordId);
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
      e.printStackTrace();
    }

    contractController.refreshTableData();
    contractController.setupPagination();
    closeWindow();
  }

  private void closeWindow() {
    stage.close();
  }
}
