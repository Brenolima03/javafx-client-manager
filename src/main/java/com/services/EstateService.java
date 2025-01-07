package com.services;

import java.util.List;

import com.db.DbException;
import com.model.dao.DaoFactory;
import com.model.dao.EstateDao;
import com.model.entities.Estate;

public class EstateService {
  private final EstateDao dao = DaoFactory.createEstateDao();

  public void insert(Estate obj) {
    dao.insertDao(obj);
  }

  public List<Estate> findAllEstates() {
    return dao.findAllEstatesDao();
  }

  public Estate findState(int id) {
    return dao.findStateDao(id);
  }

  public List<Estate> search(String filter, String argument) {
    return dao.searchDao(filter, argument);
  }

  public int count() {
    return dao.countDao();
  }

  public List<Estate> findPaginated(int page, int pageSize) {
    try {
      return dao.findPaginatedDao(page, pageSize);
    } catch (DbException e) {
      System.err.println(
        "Error fetching paginated estates: " + e.getMessage()
      );
      throw e;
    }
  }
}
