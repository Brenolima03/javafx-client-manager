package com.model.dao;

import java.util.LinkedHashMap;
import java.util.List;

import com.model.entities.Estate;

public interface EstateDao {
  void insertDao(Estate obj);
  void updateDao(int estateId, LinkedHashMap<String, Object> fieldsUpdated);
  Estate findStateDao(int id);
  List<Estate> findAllEstatesDao();
  List<Estate> searchDao(String filter, String argument);
  List<Estate> getAllClientEstatesDao(int landlordId);
  int countDao();
}
