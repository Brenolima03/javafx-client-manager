package com.model.dao;

import java.util.LinkedHashMap;
import java.util.List;

import com.model.entities.Client;

import javafx.collections.ObservableList;

public interface ClientDao {
  void insertDao(Client obj);
  void updateDao(int clientId, LinkedHashMap<String, Object> fieldsUpdated);
  void deleteByIdDao(int id, boolean isLandlord);
  Client findClientByIdDao(int id);
  List<Client> findAllDao();
  ObservableList<Client> searchDao(String filter, String argument);
  int countDao();
}
