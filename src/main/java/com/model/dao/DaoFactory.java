package com.model.dao;

import com.db.DB;
import com.model.dao.implementation.*;

public class DaoFactory {
  public static ClientDao createClientDao() {
    return new ClientDaoJDBC(DB.getConnection());
  }

  public static ContractDao createContractDao() {
    return new ContractDaoJDBC(DB.getConnection());
  }

  public static EstateDao createEstateDao() {
    return new EstateDaoJDBC(DB.getConnection());
  }
}
