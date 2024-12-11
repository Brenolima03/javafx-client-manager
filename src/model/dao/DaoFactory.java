package model.dao;

import model.dao.implementation.*;
import db.DB;

public class DaoFactory {
  public static ClientDao createClientDao() {
    return new ClientDaoJDBC(DB.getConnection());
  }
}
