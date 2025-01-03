package utils;

import gui.Alerts;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class CpfCnpj {
  public static boolean isValid(String value) {
    if (value == null || value.isEmpty()) return false;

    if (!isCpf(value) && !isCnpj(value)) {
      Alerts.showAlert(
        "CPF ou CNPJ inválido",
        null,
        "Insira um CPF ou CNPJ válido.",
        AlertType.ERROR
      );
      return false;
    }
    return true;
  }

  public static String applyCpfCnpjMaskFromDatabase(String cpfCnpj) {
    if (cpfCnpj == null || cpfCnpj.isEmpty()) return "";

    cpfCnpj = cpfCnpj.replaceAll("\\D", "");
    
    if (cpfCnpj.length() == 11)
      return cpfCnpj.replaceFirst(
        "(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4"
      );

    if (cpfCnpj.length() == 14)
      return cpfCnpj.replaceFirst(
        "(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5"
      );

    return cpfCnpj;
  }

  public static void applyCpfCnpjMaskOnInputFields(
    TextField cpfCnpjField, RadioButton cpfRadioButton,
    RadioButton cnpjRadioButton
  ) {
    cpfCnpjField.textProperty().addListener((observable, oldValue, newValue) ->
    {
      // Determine if CPF or CNPJ radio button is selected
      boolean isCpf = cpfRadioButton.isSelected();

      // Check if the user is deleting (backspace) or adding characters
      if (newValue.length() < oldValue.length())
        return; // Ignore changes caused by backspace or delete

      // Remove non-numeric characters from the new text
      String filteredText = newValue.replaceAll("[^0-9]", "");

      // Apply mask formatting dynamically
      StringBuilder formattedText = new StringBuilder();

      if (isCpf)
        for (int i = 0; i < filteredText.length(); i++) {
          char digit = filteredText.charAt(i);
          switch (i) {
            case 3:
            case 6:
              formattedText.append(".");
              formattedText.append(digit);
              break;
            case 9:
              formattedText.append("-");
              formattedText.append(digit);
              break;
            default:
              formattedText.append(digit);
              break;
          }
        }
      else
        for (int i = 0; i < filteredText.length(); i++) {
          char digit = filteredText.charAt(i);
          switch (i) {
            case 2:
            case 5:
              formattedText.append(".");
              formattedText.append(digit);
              break;
            case 8:
              formattedText.append("/");
              formattedText.append(digit);
              break;
            case 12:
              formattedText.append("-");
              formattedText.append(digit);
              break;
            default:
              formattedText.append(digit);
              break;
          }
        }
      FormFieldFormatter.updateFormattedField(
        cpfCnpjField, newValue, formattedText, isCpf ? 14 : 18
      );
    });
  }

  public static void updateCpfCnpjMask(
    RadioButton cpfRadioButton, TextField cpfCnpjField
  ) {
    if (cpfRadioButton.isSelected())
      cpfCnpjField.setPromptText("XXX.XXX.XXX-XX"); // CPF mask
    else cpfCnpjField.setPromptText("XX.XXX.XXX/XXXX-XX"); // CNPJ mask
  }

  public static boolean isCpf(String cpf) {
    cpf = cpf.replace(".", "").replace("-", "");

    if (cpf.length() != 11 || !cpf.matches("\\d+"))
      return false;

    int d1 = 0, d2 = 0, digit1, digit2, remainder;
    int cpfDigit;

    // Calculate the first verification digit
    for (int nCount = 1; nCount < cpf.length() - 1; nCount++) {
      cpfDigit = Integer.parseInt(cpf.substring(nCount - 1, nCount));
      d1 = d1 + (11 - nCount) * cpfDigit;
      d2 = d2 + (12 - nCount) * cpfDigit;
    }

    // First remainder of the division by 11
    remainder = d1 % 11;
    digit1 = (remainder < 2) ? 0 : 11 - remainder;

    d2 += 2 * digit1; // Add the first digit to the second calculation

    // Second remainder of the division by 11
    remainder = d2 % 11;
    digit2 = (remainder < 2) ? 0 : 11 - remainder;

    // Get the CPF's last two digits and compare with the calculated ones
    String validationDigits = cpf.substring(cpf.length() - 2);
    String resultDigit = String.valueOf(digit1) + digit2;

    // Return true if the digits match
    return validationDigits.equals(resultDigit);
  }

  public static boolean isCnpj(String cnpj) {
    cnpj = cnpj.replace(".", "").replace("-", "").replace("/", "");

    // Check if CNPJ length is 14 and contains only digits
    if (cnpj.length() != 14 || !cnpj.matches("\\d+")) return false;

    // Consider it an error if the CNPJ consists of identical digits
    if (
      cnpj.equals("00000000000000") || cnpj.equals("11111111111111") ||
      cnpj.equals("22222222222222") || cnpj.equals("33333333333333") ||
      cnpj.equals("44444444444444") || cnpj.equals("55555555555555") ||
      cnpj.equals("66666666666666") || cnpj.equals("77777777777777") ||
      cnpj.equals("88888888888888") || cnpj.equals("99999999999999")
    ) {
      return false;
    }

    char dig13, dig14;
    int sum, i, remainder, num, weight;

    // Calculate the first verification digit
    sum = 0;
    weight = 2;
    for (i = 11; i >= 0; i--) {
      num = (int)(cnpj.charAt(i) - '0');
      sum = sum + (num * weight);
      weight = (weight == 9) ? 2 : weight + 1;
    }

    remainder = sum % 11;
    dig13 =
      (remainder == 0 || remainder == 1) ? '0' : (char)('0' + (11 - remainder));

    // Calculate the second verification digit
    sum = 0;
    weight = 2;
    for (i = 12; i >= 0; i--) {
      num = (int)(cnpj.charAt(i) - '0');
      sum = sum + (num * weight);
      weight = (weight == 9) ? 2 : weight + 1;
    }

    remainder = sum % 11;
    dig14 =
      (remainder == 0 || remainder == 1) ? '0' : (char)('0' + (11 - remainder));

    // Compare the calculated digits with the given digits
    return dig13 == cnpj.charAt(12) && dig14 == cnpj.charAt(13);
  }
}
