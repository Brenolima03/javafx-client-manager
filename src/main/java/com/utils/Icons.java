package com.utils;

import java.io.File;

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
}
