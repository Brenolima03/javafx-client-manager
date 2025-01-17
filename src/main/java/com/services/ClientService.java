package com.services;

import java.util.List;

import com.db.DbException;
import com.gui.Alerts;
import com.model.dao.ClientDao;
import com.model.dao.DaoFactory;
import com.model.entities.Client;

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

  public void update(Client obj) {
    dao.updateDao(obj);
  }

  public void delete(int id, boolean isLandlord, boolean usedGuarantor) {
    dao.deleteByIdDao(id, isLandlord, usedGuarantor);
  }

  public List<Client> search(String filter, String argument) {
    return dao.searchDao(filter, argument);
  }

  public List<Client> findPaginated(int page, int pageSize) {
    try {
      return dao.findPaginatedDao(page, pageSize);
    } catch (DbException e) {
      Alerts.showAlert(
      "Erro ao buscar ", e.getMessage(), null, AlertType.ERROR
      );
      throw e;
    }
  }

  public List<String> getGuarantorsById(int id) {
    try {
      return dao.getGuarantorsById(id);
    } catch (DbException e) {
      throw new DbException(
        "Error while retrieving guarantors for client: " + id, e
      );
    }
  }

  public String getGuaranteeTypeByContractId(int id) {
    try {
      return dao.getGuaranteeTypeByContractIdDao(id);
    } catch (DbException e) {
    	throw new DbException(
    	  "Error while retrieving guarantee type for client: " + id, e
      );
    }
  }

  public double getDeposit(int contract) {
    try {
      return dao.getDeposit(contract);
    } catch (DbException e) {
    	throw new DbException(
    	  "Error while retrieving deposit for client: " + contract, e
      );
    }
  }

  public int count() {
    return dao.countDao();
  }
}
