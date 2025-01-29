package com.services;

import java.util.LinkedHashMap;
import java.util.List;

import com.db.DbException;
import com.gui.Alerts;
import com.model.dao.ClientDao;
import com.model.dao.DaoFactory;
import com.model.entities.Client;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;

public class ClientService {
  private final ClientDao dao = DaoFactory.createClientDao();

  public void insert(Client obj) {
    dao.insertDao(obj);
  }

  public Client findClientById(int id) {
    return dao.findClientByIdDao(id);
  }

  public List<Client> findAllClients() {
    return dao.findAllDao();  
  }

  public void update(int clientId, LinkedHashMap<String, Object> fieldsUpdated){
    dao.updateDao(clientId, fieldsUpdated);
  }

  public void delete(int id, boolean isLandlord) {
    dao.deleteByIdDao(id, isLandlord);
  }

  public ObservableList<Client> search(String filter, String argument) {
    return dao.searchDao(filter, argument);
  }

  public int count() {
    return dao.countDao();
  }
}
