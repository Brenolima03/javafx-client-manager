package utils;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class Currency {
  // Custom StringConverter for handling "R$" currency formatting
  public static StringConverter <Double> getCurrencyConverter() {
    return new StringConverter <Double> () {
      @Override
      public String toString(Double value) {
        if (value == null) return "R$ 0,00"; // Default when value is null
        return String.format("R$ %.2f", value); // Format as currency
      }

      @Override
      public Double fromString(String string) {
        // Remove any characters that are not digits or the decimal separator
        String value = string.replaceAll("[^\\d.,]", "").replace(",", ".");
        try {
          // Parse the cleaned string to a Double
          return Double.parseDouble(value);
        } catch (NumberFormatException e) {
          return 0.0; // Return 0.0 if the input is not valid
        }
      }
      
    };
  }

  public static TextFormatter<String> allowOnlyDigitsAndSeparators() {
    return new TextFormatter<>(change -> {
      String text = change.getControlNewText();

      // Reject if modifying "R$ " prefix or invalid characters
      if (
        change.getRangeStart() < 3 || text.length() < 3 ||
        !text.startsWith("R$ ") || !text.substring(3).matches("^\\d*[.,]?\\d*$")
      ) return null;

      String numberPart = text.substring(3);

      if (
        numberPart.startsWith(".") || numberPart.startsWith(",") ||
        (numberPart.contains(",") && change.getText().contains(".")) ||
        (numberPart.contains(".") && change.getText().contains(","))
      ) return null;

      // Limit decimal places to 2
      if ((numberPart.contains(".") || numberPart.contains(","))) {
        String[] parts = numberPart.split("[.,]");

        if (parts.length > 1 && parts[1].length() > 2) return null;
      }
      return change;
    });
  }

  public static double parseMonetaryValue(String value) {
	    // Remove all non-digit characters (except the decimal separator)
	    String cleanedValue = value.replaceAll("[^\\d.,]", "").replace(",", ".");
	    try {
	      return Double.parseDouble(cleanedValue);
	    } catch (NumberFormatException e) {
	      return 0.0; // Default to 0.0 if parsing fails
	    }
	  }
}
