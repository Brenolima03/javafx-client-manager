package model.dao;

import java.util.List;
import model.entities.Client;

public interface ClientDao {
  void insertDao(Client obj);
  String findClientByIdDao(int id);
  List<Client> findAllDao();
  void updateDao(Client obj);
  void deleteByIdDao(int id);
  List<Client> findPaginatedDao(int page, int pageSize);
  List<Client> searchDao(String filter, String argument);
  List<String> getGuarantorsById(int id);
  List<String> findStatesDao();
  String getGuaranteeTypeByContractIdDao(int id);
  double getDeposit(int contract);
  int countDao();
}
