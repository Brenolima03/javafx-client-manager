package com.model.dao;

import java.util.List;

import com.model.entities.Client;

public interface ClientDao {
  void insertDao(Client obj);
  Client findClientByIdDao(int id);
  List<Client> findAllDao();
  void updateDao(Client obj);
  void deleteByIdDao(int id, boolean isLandlord, boolean usedGuarantor);
  List<Client> findPaginatedDao(int page, int pageSize);
  List<Client> searchDao(String filter, String argument);
  List<String> getGuarantorsById(int id);
  String getGuaranteeTypeByContractIdDao(int id);
  double getDeposit(int contract);
  int countDao();
}
