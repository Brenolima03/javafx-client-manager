module clientManager {
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.fxml;
  requires javafx.base;
  requires java.sql;

  exports model.dao.implementation;
  exports model.entities;
  exports application;
  exports gui;

  opens application to javafx.fxml;
  opens gui to javafx.fxml;
}
