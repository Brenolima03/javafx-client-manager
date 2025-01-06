package com.utils;

import java.util.List;

import javafx.scene.control.ComboBox;

public class States {
    public static void populateStateCombobox(ComboBox stateField) {
    List<String> states = List.of(
      "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO",
      "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR",
      "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"
    );
    stateField.getItems().addAll(states);
    stateField.setPromptText("Selecione o estado");
  }
}
