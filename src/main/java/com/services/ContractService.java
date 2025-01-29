package com.services;

import java.util.List;

import com.model.dao.ContractDao;
import com.model.dao.DaoFactory;
import com.model.entities.Contract;

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

  public List<Contract> getContractsByDate(String startDate, String endDate) {
    return dao.getContractsByDateDao(startDate, endDate);
  }

  public String getClient(int clientId) {
    return dao.getClientDao(clientId);
  }

  public List<String> getGuarantors(int guarantorId) {
    return dao.getGuarantorsDao(guarantorId);
  }
}
