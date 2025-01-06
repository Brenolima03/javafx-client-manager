package com.utils;

import javafx.scene.control.TextField;

public class FormFieldFormatter {
	public static void updateFormattedField(
    TextField field, String newValue, StringBuilder formattedText, int maxLength
  ) {
    // Restrict to the specified maximum length
    if (formattedText.length() > maxLength) {
      formattedText.setLength(maxLength);
    }

    // Update the text field with the formatted text only if it's different
    if (!newValue.equals(formattedText.toString())) {
      field.setText(formattedText.toString());

      // Ensure caret stays at the correct position (end of the text)
      field.positionCaret(formattedText.length());
    }
  }
}
