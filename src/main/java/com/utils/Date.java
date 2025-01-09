package com.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

public class Date {
  private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public static StringConverter<LocalDate> getDateConverter() {
    return new StringConverter<LocalDate>() {
      @Override
      public String toString(LocalDate date) {
        // Format the LocalDate to the "dd/MM/yyyy" format
        return date != null ? DATE_FORMATTER.format(date) : "";
      }

      @Override
      public LocalDate fromString(String string) {
        try {
          // Try to parse the input string into a LocalDate
          return string != null && !string.isEmpty() ?
            LocalDate.parse(string, DATE_FORMATTER) : null;
        } catch (DateTimeParseException e) {
          // If the input is invalid, parsing will fail and return null
          return null;
        }
      }
    };
  }

  public static void applyDateMaskOnInputFields(DatePicker date) {
    date.getEditor().textProperty().addListener(
    (observable, oldValue, newValue) -> {
      // Check if the user is deleting (backspace) or adding characters
      if (newValue.length() < oldValue.length())
        return; // Ignore changes caused by backspace or delete

      // Remove non-numeric characters from the new text
      String filteredText = newValue.replaceAll("[^0-9]", "");

      // Apply mask formatting dynamically
      StringBuilder formattedText = new StringBuilder();

        for (int i = 0; i < filteredText.length(); i++) {
          char digit = filteredText.charAt(i);
          switch (i) {
            case 2, 4 -> {
              formattedText.append("/");
              formattedText.append(digit);
              }
            default -> formattedText.append(digit);
          }
        }
      FormFieldFormatter.updateFormattedField(
        date.getEditor(), newValue, formattedText, 10
      );
    });
  }

  public static boolean isValidDate(LocalDate date) {
    try {
      if (date == null) return false;
      date.format(DateTimeFormatter.ISO_LOCAL_DATE);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}
