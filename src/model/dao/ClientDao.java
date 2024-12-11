package model.dao;

import java.util.List;
import model.entities.Client;

public interface ClientDao {
  void insertDao(Client obj);
  void updateDao(Client obj);
  void deleteByIdDao(int id);
  int countDao();
  List<Client> searchDao(String filter, String argument);
  List<Client> findPaginatedDao(int page, int pageSize);
  List<String> getContractsByCpfCnpjDao(String cpfCnpj);
  List<String> getGuarantorsByCpfCnpjDao(String cpfCnpj);
}
