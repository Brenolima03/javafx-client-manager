package com.model.dao;

import java.util.List;

import com.model.entities.Contract;

public interface ContractDao {
  void insertDao(Contract obj);
  List<Contract> getAllContractsDao();
  List<Contract> getContractsByDateDao(String startDate, String endDate);
  List<Contract> searchDao(String filter, String argument);
  List<String> getGuarantorsDao(int guarantorId);
  String getClientDao(int clientId);
  int countDao();
}
