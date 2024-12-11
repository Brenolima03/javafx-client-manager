package utils;

import javafx.util.StringConverter;

public class Currency {
  // Custom StringConverter for handling "R$" currency formatting
  public static StringConverter < Double > getCurrencyConverter() {
    return new StringConverter < Double > () {
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
}
