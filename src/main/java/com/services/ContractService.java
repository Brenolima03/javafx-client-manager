package com.services;

import java.util.List;

import com.db.DbException;
import com.model.dao.ContractDao;
import com.model.dao.DaoFactory;
import com.model.entities.Contract;

public class ContractService {
  private final ContractDao dao = DaoFactory.createContractDao();

  public void insert(Contract obj) {
	  dao.insertDao(obj);
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
      System.err.println(
        "Error fetching paginated contracts: " + e.getMessage()
      );
      throw e;
    }
  }
}