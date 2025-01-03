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

  public String findClientById(int id) {
    return dao.findClientByIdDao(id);
  }

  public List<Client> findAllClients() {
    return dao.findAllDao();  
  }

  public void update(Client obj) {
    dao.updateDao(obj);
  }

  public void delete(int id) {
    dao.deleteByIdDao(id);
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

  public List<String> getGuarantorsById(int id) {
    try {
      return dao.getGuarantorsById(id);
    } catch (DbException e) {
      throw new DbException(
        "Error while retrieving guarantors for client: " + id, e
      );
    }
  }

  public String getGuaranteeTypeByContractId(int id) {
    try {
      return dao.getGuaranteeTypeByContractIdDao(id);
    } catch (DbException e) {
    	throw new DbException(
    	  "Error while retrieving guarantee type for client: " + id, e
      );
    }
  }

  public double getDeposit(int contract) {
    try {
      return dao.getDeposit(contract);
    } catch (DbException e) {
    	throw new DbException(
    	  "Error while retrieving deposit for client: " + contract, e
      );
    }
  }

  public int count() {
    return dao.countDao();
  }

  public List<String> findStates() {
    return dao.findStatesDao();  
  }
}
