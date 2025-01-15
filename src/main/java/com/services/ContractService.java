package com.services;

import java.util.List;

import com.db.DbException;
import com.gui.Alerts;
import com.model.dao.ContractDao;
import com.model.dao.DaoFactory;
import com.model.entities.Contract;

import javafx.scene.control.Alert.AlertType;

public class ContractService {
  private final ContractDao dao = DaoFactory.createContractDao();

  public void insert(Contract obj) {
	  dao.insertDao(obj);
  }

  public List<Contract> getAllContracts() {
    return dao.getAllContractsDao();
  }

  public List<Contract> search(String filter, String argument) {
    return dao.searchDao(filter, argument);
  }

  public int count() {
    return dao.countDao();
  }

  public List<Contract> findPaginated(int page, int pageSize) {
    try {
      return dao.findPaginatedDao(page, pageSize);
    } catch (DbException e) {
      Alerts.showAlert(
      "Erro ao buscar ", e.getMessage(), null, AlertType.ERROR
      );
      throw e;
    }
  }

  public List<Contract> getContractsByDate(String startDate, String endDate) {
    return dao.getContractsByDateDao(startDate, endDate);
  }
}
