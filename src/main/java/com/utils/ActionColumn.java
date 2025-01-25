package com.utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

public class ActionColumn {
  public <S> void setupActionColumn(
    TableColumn<S, String> updateDeleteColumn,
    EventHandler<ActionEvent> editEventHandler,
    EventHandler<ActionEvent> deleteEventHandler,
    boolean canDelete
  ) {
    updateDeleteColumn.setCellFactory(param -> new TableCell<S, String>() {
      private final Button editButton = new Button();
      private final Button deleteButton = canDelete ? new Button() : null;
      private final HBox actionBox = new HBox(19);

      {
        Icons.setupTransparentButton(
          editButton, "icons/update-icon.png", editEventHandler);
        actionBox.getChildren().add(editButton);

        if (canDelete) {
          Icons.setupTransparentButton(
            deleteButton, "icons/delete-icon.png", deleteEventHandler
          );
          actionBox.getChildren().add(deleteButton);
        }
        actionBox.setAlignment(Pos.CENTER);
      }

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : actionBox);
      }
    });
  }

  public <S> void handleEdit(
    ActionEvent event, S entity, EventHandler<ActionEvent> editService
  ) {
    editService.handle(event);
  }

  public <S> void handleDelete(
    ActionEvent event, S entity, EventHandler<ActionEvent> deleteService
  ) {
    deleteService.handle(event);
  }
}
