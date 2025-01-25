package com.utils;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Icons {
  public static void setIcon(Stage stage, String imagePath) {
    File imageFile = new File(imagePath);
    if (imageFile.exists()) {
      Image icon = new Image(imageFile.toURI().toString(), 64, 64, true, true);
      stage.getIcons().add(icon);
    }
  }

  public static void setButtonIcon(Button button, String imagePath) {
    File imageFile = new File(imagePath);
    if (imageFile.exists()) {
      Image icon = new Image(imageFile.toURI().toString(), 16, 16, true, true);
      ImageView iconView = new ImageView(icon);
      button.setGraphic(iconView);
    }
  }

  public static void setupTransparentButton(
    Button button, String path, EventHandler<ActionEvent> event
  ) {
    Icons.setButtonIcon(button, path);
    button.setPrefWidth(38);
    button.setPrefHeight(38);
    button.setOnAction(event);
    button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
  }
}
