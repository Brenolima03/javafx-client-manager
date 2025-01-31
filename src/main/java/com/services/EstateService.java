package com.services;

import java.util.LinkedHashMap;
import java.util.List;

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

  public void update(int estateId, LinkedHashMap<String, Object> fieldsUpdated){
    dao.updateDao(estateId, fieldsUpdated);
  }

  public Estate findState(int id) {
    return dao.findStateDao(id);
  }

  public List<Estate> search(String filter, String argument) {
    return dao.searchDao(filter, argument);
  }

  public List<Estate> getAllClientEstates(int landlordId) {
    return dao.getAllClientEstatesDao(landlordId);
  }

  public int count() {
    return dao.countDao();
  }
}
