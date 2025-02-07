package com.utils;

import javafx.scene.control.TextField;

public class TelephoneMask {
  public static String applyTelephoneMaskFromDatabase(String telephone) {
    if (telephone == null || telephone.isEmpty()) return "";

    telephone = telephone.replaceAll("\\D", "");

    if (telephone.length() == 11)
      return telephone.replaceFirst(
        "(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3"
      );

    if (telephone.length() == 10)
      return telephone.replaceFirst(
        "(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3"
      );

    return telephone;
  }

  public static void applyTelephoneMask(TextField telephoneField) {
    telephoneField.textProperty().addListener(
    (observable, oldValue, newValue) -> {
      if (newValue.length() < oldValue.length()) return;

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
