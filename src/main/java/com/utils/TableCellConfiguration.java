package com.utils;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.ss.formula.functions.T;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class TableCellConfiguration {
  private void blockKeyboardExceptCtrlC(Node node) {
    node.addEventFilter(KeyEvent.ANY, event -> {
      if (!(event.isControlDown() && event.getCode() == KeyCode.C))
        event.consume();
    });
  }

  public static <S> void setupComboBoxColumn(
    TableView<S> table, TableColumn <S, List <String>> column,
    String styleClass, Function <S, List <String>> getterMethod
  ) {
    column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
      getterMethod.apply(cellData.getValue())
    ));

    column.setCellFactory(col -> new TableCell<S, List<String>>() {
      private final ComboBox<String> comboBox = new ComboBox<>();

      @Override
      protected void updateItem(List<String> items, boolean empty) {
        super.updateItem(items, empty);

        if (empty || items == null) {
          setGraphic(null);
        } else {
          comboBox.getItems().setAll(items);
          comboBox.setValue(items.isEmpty() ? null : items.get(0));
          comboBox.getStyleClass().add(styleClass);

          setGraphic(comboBox);
          comboBox.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (newFocus) table.getSelectionModel().select(getIndex());
          });
        }
      }
    });
  }

  public <S, T> void configureCellFactory(
    TableColumn<S, T> column, int width, String property,
    CustomContextMenu contextMenu
  ) {
    column.setMaxWidth(width);
    column.setMinWidth(width);
    column.setPrefWidth(width);
    column.setCellValueFactory(new PropertyValueFactory<>(property));
    column.setCellFactory(tc -> {
      TableCell<S, T> cell = new TableCell<>() {
        private TextField textField;

        @Override
        protected void updateItem(T item, boolean empty) {
          super.updateItem(item, empty);
          if (item == null || empty) {
            setGraphic(null);
          } else if (item instanceof LocalDate) {
            setText(Date.getDateConverter().toString((LocalDate) getItem()));
          } else {
            setText(item.toString());
          }
        }
        @Override
        public void startEdit() {
          if (getItem() == null || getItem().toString().isEmpty()) return;

          super.startEdit();

          textField = new TextField(getItem() instanceof LocalDate
            ? Date.getDateConverter().toString((LocalDate) getItem())
            : getItem().toString());
          setText(null);
          setGraphic(textField);
          contextMenu.setCustomContextMenuForTableCells(textField);
        }

        @Override
        public void cancelEdit() {
          super.cancelEdit();
          setGraphic(null);
          setText(getItem() == null 
            ? "" 
            : (getItem() instanceof LocalDate 
              ? Date.getDateConverter().toString((LocalDate) getItem()) 
              : getItem().toString()));
        }
      };
      blockKeyboardExceptCtrlC(cell);
      return cell;
    });
  }
}
