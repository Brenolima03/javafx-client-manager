package utils;

import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import java.time.LocalDate;
import gui.Alerts;

public class Date {
  private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy");

  // Returns a StringConverter for LocalDate with the "dd/MM/yyyy" format
  public static StringConverter<LocalDate> getDateConverter() {
    return new StringConverter<LocalDate>() {
      @Override
      public String toString(LocalDate date) {
        // Format as dd/MM/yyyy
        return date != null ? date.format(DATE_FORMATTER) : "";
      }

      @Override
      public LocalDate fromString(String string) {
        try {
          // Try to parse the input date using the formatter
          return LocalDate.parse(string, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
  	      Alerts.showAlert(
            "Data inválida",
            null,
            "Insira uma data no formato dd/MM/aaaa",
            AlertType.ERROR
          );
          return null; // If parsing fails, just return null and show an alert
        }
      }
    };
  }

  // Validates if a LocalDate is valid (just checks if it's not null)
  public static boolean isValidDate(LocalDate date) {
    return date != null;
  }
}
