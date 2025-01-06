module com.application {
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.fxml;
  requires javafx.base;
  requires java.sql;

  opens com.gui to javafx.fxml; 

  exports com.application;
  exports com.model.entities;
  exports com.model.dao.implementation;
  exports com.model.dao;
  exports com.gui;
}
