package utils;

import javafx.scene.control.TextField;

public class TelephoneMask {
  public static void applyTelephoneMask(TextField telephoneField) {
    telephoneField.textProperty().addListener(
    (observable, oldValue, newValue) -> {
      // Check if the user is deleting (backspace) or adding characters
      if (newValue.length() < oldValue.length()) {
        return; // Ignore changes caused by backspace or delete
      }

      // Remove non-numeric characters from the new text
      String filteredText = newValue.replaceAll("[^0-9]", "");

      // Apply mask formatting dynamically
      StringBuilder formattedText = new StringBuilder();

      for (int i = 0; i < filteredText.length(); i++) {
        char digit = filteredText.charAt(i);
        switch (i) {
          case 0:
            formattedText.append("(").append(digit);
            break;
          case 1:
            formattedText.append(digit).append(") ");
            break;
          case 6:
            formattedText.append(digit).append("-");
            break;
          default:
            formattedText.append(digit);
            break;
        }
      }

      FormFieldFormatter.updateFormattedField(
        telephoneField, newValue, formattedText, 15
      );
    });
  }
}
