package com.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class CustomContextMenu {
  private final ContextMenu contextMenu;
  private final MenuItem clearItem;
  private final MenuItem cutItem;
  private final MenuItem copyItem;
  private final MenuItem pasteItem;
  private TextField ownerField;

  public CustomContextMenu() {
    this.contextMenu = new ContextMenu();

    clearItem = new MenuItem("Limpar");
    cutItem = new MenuItem("Recortar");
    copyItem = new MenuItem("Copiar");
    pasteItem = new MenuItem("Colar");

    String style = "-fx-font-size: 20px;";
    clearItem.setStyle(style);
    cutItem.setStyle(style);
    copyItem.setStyle(style);
    pasteItem.setStyle(style);

    clearItem.setOnAction(e -> {
      if (ownerField != null) ownerField.clear();
    });
    cutItem.setOnAction(e -> {
      if (ownerField != null) ownerField.cut();
    });
    copyItem.setOnAction(e -> {
      if (ownerField != null) ownerField.copy();
    });
    pasteItem.setOnAction(e -> {
      if (ownerField != null) ownerField.paste();
    });

    contextMenu.getItems().addAll(clearItem, cutItem, copyItem, pasteItem);
  }

  public void setCustomContextMenuForTextFields(TextField textField) {
    this.ownerField = textField;
    contextMenu.getItems().clear();
    contextMenu.getItems().addAll(clearItem, cutItem, copyItem, pasteItem);
    textField.setContextMenu(contextMenu);
  }

  public void setCustomContextMenuForTableCells(TextField textField) {
    this.ownerField = textField;
    contextMenu.getItems().clear();
    contextMenu.getItems().addAll(copyItem);
    textField.setContextMenu(contextMenu);
  }
  
  public ContextMenu getContextMenu() {
    return contextMenu;
  }

  public ObservableList<MenuItem> getMenuItems() {
    return contextMenu.getItems();
  }
}
