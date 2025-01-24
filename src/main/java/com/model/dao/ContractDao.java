package com.model.dao;

import java.util.List;

import com.model.entities.Contract;

public interface ContractDao {
  void insertDao(Contract obj);
  List<Contract> findPaginatedDao(int page, int pageSize);
  List<Contract> getAllContractsDao();
  List<Contract> getContractsByDateDao(String startDate, String endDate);
  List<Contract> searchDao(String filter, String argument);
  int countDao();
}
