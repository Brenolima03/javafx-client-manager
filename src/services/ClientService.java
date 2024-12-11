package services;

import model.entities.Client;
import model.dao.DaoFactory;
import model.dao.ClientDao;
import java.util.List;
import db.DbException;

public class ClientService {
  private ClientDao dao = DaoFactory.createClientDao();

  public void insert(Client obj) {
    dao.insertDao(obj);
  }

  public void update(Client obj) {
    dao.updateDao(obj);
  }

  public void delete(int id) {
    dao.deleteByIdDao(id);
  }

  public int count() {
    return dao.countDao();
  }

  public List<Client> search(String filter, String argument) {
    return dao.searchDao(filter, argument);
  }

  public List<Client> findPaginated(int page, int pageSize) {
    try {
      return dao.findPaginatedDao(page, pageSize);
    } catch (DbException e) {
      System.err.println("Error fetching paginated clients: " + e.getMessage());
      throw e;
    }
  }

  public List<String> getContractsByClientCpfCnpj(String cpfCnpj) {
    try {
      return dao.getContractsByCpfCnpjDao(cpfCnpj);
    } catch (DbException e) {
      throw new DbException(
        "Error while retrieving contracts for client: " + cpfCnpj, e
      );
    }
  }
  public List<String> getGuarantorsByClientCpfCnpj(String cpfCnpj) {
    try {
      return dao.getGuarantorsByCpfCnpjDao(cpfCnpj);
    } catch (DbException e) {
      throw new DbException(
        "Error while retrieving guarantors for client: " + cpfCnpj, e
      );
    }
  }
}
