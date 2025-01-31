package com.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.db.DbException;
import com.model.entities.Estate;
import com.services.EstateService;
import com.utils.CustomContextMenu;
import com.utils.Icons;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateEstateFormController {
  private int estateId;
  private Stage stage;
  private EstateService estateService;
  private EstateListController estateListController;

  @FXML
  private TextField descriptionField;

  @FXML
  private Button saveButton;

  @FXML
  private Button cancelButton;

  public void setStage(Stage stage) {
    this.stage = stage;
    Icons.setIcon(stage, "icons/favicon.png");
  }

  public void setEstateService(EstateService estateService) {
    this.estateService = estateService;
  }

  public void setEstateListController(EstateListController estateListController)
  {
    this.estateListController = estateListController;
  }

  public void setEstateId(int estateId) {
    this.estateId = estateId;
  }

  private void closeWindow() {
    stage.close();
  }

  private void handleSave() {
    Map<String, String> fieldMap = Map.of(
      "Descrição", descriptionField.getText().trim()
    );

    // Create a map for the updated fields
    LinkedHashMap<String, Object> updatedFields = new LinkedHashMap<>();
    updatedFields.put("description", fieldMap.get("Descrição"));

    updatedFields
      .values().removeIf(value -> value == null || value.toString().isEmpty());

    try {
      estateService.update(estateId, updatedFields);

      closeWindow();
      estateListController.setupPagination(
        FXCollections.observableArrayList(estateService.findAllEstates())
      );
    } catch (DbException e) {
      Alerts.showAlert("Erro ao salvar", e.getMessage(), null, AlertType.ERROR);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void initialize() {
    if (estateService == null)
      throw new IllegalStateException(
        "EstateService was not initialized. " +
        "Call setEstateService() before loading the controller."
      );

    CustomContextMenu contextMenu = new CustomContextMenu();

    descriptionField.focusedProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue)
        contextMenu.setCustomContextMenuForTextFields(descriptionField);
    });

    saveButton.setOnAction(event -> handleSave());
    cancelButton.setOnAction(event -> closeWindow());
  }
}
