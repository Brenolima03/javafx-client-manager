package com.model.dao;

import java.util.List;

import com.model.entities.Estate;

public interface EstateDao {
  void insertDao(Estate obj);
  Estate findStateDao(int id);
  List<Estate> findAllEstatesDao();
  List<Estate> findPaginatedDao(int page, int pageSize);
  List<Estate> searchDao(String filter, String argument);
  int countDao();
}
