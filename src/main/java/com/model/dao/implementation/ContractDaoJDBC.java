package com.model.dao.implementation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.db.DB;
import com.db.DbException;
import com.model.dao.ContractDao;
import com.model.entities.Contract;
import com.model.entities.Guarantee;

public class ContractDaoJDBC implements ContractDao {
  private final Connection conn;

  public ContractDaoJDBC(Connection conn) {
    this.conn = conn;
  }

  @Override
  public void insertDao(Contract obj) {
    PreparedStatement st = null;
    ResultSet generatedKeys = null;

    try {
      int tenantId = obj.getTenant();
      int landlordId = obj.getLandlord();
      int estateId = obj.getEstate();
      String fileBase64 = obj.getFileBase64();
      Date rentBeginning = obj.getRentBeginning() != null ? 
        Date.valueOf(obj.getRentBeginning()) : null;
      Date rentEnd = obj.getRentEnd() != null ? 
        Date.valueOf(obj.getRentEnd()) : null;
      double rentValue = obj.getRentValue();
      double deposit = obj.getDepositValue();
      String energyConsumerUnit = obj.getEnergyConsumerUnit();
      String waterRegistrationNumber = obj.getWaterRegistrationNumber();
      LocalDate signingDate = obj.getContractSigningDate();
      Guarantee guarantee = obj.getGuarantee();
      String guaranteeType = null;
      int guarantorId = 0;

      if (guarantee != null) {
        guaranteeType = guarantee.getGuaranteeType() != null ? 
        guarantee.getGuaranteeType().name() : null;

        if (
          Guarantee.GuaranteeType.GUARANTOR.equals(
            guarantee.getGuaranteeType()
          )
        ) guarantorId = insertGuarantor(tenantId, guarantee);
      }

      // SQL query to insert contract
      String sql = """
        INSERT INTO CONTRACTS (
          TENANT_ID, LANDLORD_ID, ESTATE_ID, FILE_BASE64, RENT_BEGINNING,
          RENT_END, RENT_VALUE, ENERGY_CONSUMER_UNIT, WATER_REGISTRATION_NUMBER,
          GUARANTOR_ID, CONTRACT_SIGNING_DATE, DEPOSIT, GUARANTEE_TYPE
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

      st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      st.setInt(1, tenantId);
      st.setInt(2, landlordId);
      st.setInt(3, estateId);
      st.setString(4, fileBase64);
      st.setDate(5, rentBeginning);
      st.setDate(6, rentEnd);
      st.setDouble(7, rentValue);
      st.setString(8, energyConsumerUnit);
      st.setString(9, waterRegistrationNumber);
      st.setInt(10, guarantorId);
      st.setDate(11, signingDate != null ? Date.valueOf(signingDate) : null);
      st.setDouble(12, deposit);
      st.setString(13, guaranteeType);

      int rowsAffected = st.executeUpdate();

      if (rowsAffected == 0)
        throw new DbException(
          "Unexpected error! No rows affected while inserting contract."
        );

      // Get the generated contract id
      generatedKeys = st.getGeneratedKeys();
      if (generatedKeys.next()) {
        int contractId = generatedKeys.getInt(1);
        // Set the generated contract id back into the object
        obj.setId(contractId);
      }

      insertClientContract(tenantId, obj.getId());
      insertClientContract(landlordId, obj.getId());
      insertTenantToEstate(tenantId, estateId);

    } catch (SQLException e) {
      throw new DbException(
        "Error during contract insertion: " + e.getMessage(), e
      );
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(generatedKeys);
    }
  }

  // Helper method to insert into CLIENT_CONTRACT table
  private void insertClientContract(int clientId, int contractId) {
    PreparedStatement st = null;
    try {
      String sql =
        "INSERT INTO CLIENT_CONTRACT (CLIENT_ID, CONTRACT_ID) VALUES (?, ?)";
      st = conn.prepareStatement(sql);
      st.setInt(1, clientId);
      st.setInt(2, contractId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new DbException(
        "Error inserting into CLIENT_CONTRACT table: " + e.getMessage(), e
      );
    } finally {
      DB.closeStatement(st);
    }
  }

  private int insertGuarantor(int clientId, Guarantee guarantee)
    throws SQLException, DbException {
    PreparedStatement st = null;
    ResultSet generatedKeys = null;

    try {
      List<String> guarantors = guarantee.getGuarantorNames();
      String guarantorName = guarantors.get(0);
      String partnerName = guarantors.size() > 1 ? guarantors.get(1) : null;
      boolean isMarried = partnerName != null && !partnerName.trim().isEmpty();

      String sql = """
        INSERT INTO GUARANTORS (TENANT_ID, GUARANTOR_NAME, PARTNER_NAME, 
        IS_SPONSOR_MARRIED) VALUES (?, ?, ?, ?)  
      """;

      st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      st.setInt(1, clientId);
      st.setString(2, guarantorName);
      st.setString(3, partnerName);
      st.setBoolean(4, isMarried);

      int rowsAffected = st.executeUpdate();
      if (rowsAffected == 0)
        throw new DbException(
          "Unexpected error! No rows affected while inserting guarantor."
        );

      generatedKeys = st.getGeneratedKeys();
      if (generatedKeys.next()) return generatedKeys.getInt(1);
      else throw new DbException("Failed to retrieve guarantor ID.");

    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(generatedKeys);
    }
  }

  private int insertTenantToEstate(int tenantId, int estateId)
  throws SQLException, DbException {
    PreparedStatement st = null;
    ResultSet generatedKeys = null;
    try {
      String sql = " UPDATE ESTATES SET TENANT_ID = ? WHERE ID = ?";

      st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      st.setInt(1, tenantId);
      st.setInt(2, estateId);

      int affectedRows = st.executeUpdate();

      if (affectedRows > 0) {
        generatedKeys = st.getGeneratedKeys();
        if (generatedKeys.next()) return generatedKeys.getInt(1);
      }
      return 0;
    } catch (SQLException e) {
      throw new DbException(
        "Error during tenant insertion into estate: " + e.getMessage(), e
      );
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(generatedKeys);
    }
  }  

  private Contract instantiateContractDao(ResultSet rs) throws SQLException {
    Contract contract = new Contract();
    try {
      contract.setId(rs.getInt("ID"));
      contract.setTenant(rs.getInt("TENANT_ID"));
      contract.setLandlord(rs.getInt("LANDLORD_ID"));
      contract.setEstate(rs.getInt("ESTATE_ID"));
      contract.setGuarantor(rs.getInt("GUARANTOR_ID"));
      contract.setRentBeginning(rs.getDate("RENT_BEGINNING").toLocalDate());
      contract.setRentEnd(rs.getDate("RENT_END").toLocalDate());
      contract.setContractSigningDate(
        rs.getDate("CONTRACT_SIGNING_DATE").toLocalDate()
      );
      contract.setRentValue(rs.getDouble("RENT_VALUE"));
      contract.setDepositValue(rs.getDouble("DEPOSIT"));
      contract.setEnergyConsumerUnit(rs.getString("ENERGY_CONSUMER_UNIT"));
      contract.setWaterRegistrationNumber(
        rs.getString("WATER_REGISTRATION_NUMBER")
      );

      // Instantiate the Guarantee object
      Guarantee guarantee = new Guarantee();

      // Get the guarantee type as a string from the ResultSet
      String guaranteeTypeString = rs.getString("GUARANTEE_TYPE");
      // Convert the string to the corresponding GuaranteeType enum
      Guarantee.GuaranteeType guaranteeType =
        Guarantee.GuaranteeType.valueOf(guaranteeTypeString.toUpperCase());

      // Set the guarantee type in the Guarantee object
      guarantee.setGuaranteeType(guaranteeType);

      // Set the Guarantee object in the contract
      contract.setGuarantee(guarantee);
    } catch (SQLException e) {
      // Ignore exceptions and continue
      System.err.println(
        "Error while instantiating contract: " + e.getMessage()
      );
    }

    return contract;
  }

  @Override
  public List<Contract> getAllContractsDao() {
    List<Contract> contracts = new ArrayList<>();

    try (
      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM CONTRACTS");
      ResultSet rs = stmt.executeQuery()
    ) {

      while (rs.next()) {
        Contract contract = instantiateContractDao(rs);
        contracts.add(contract);
      }
    } catch (SQLException e) {
    }

    return contracts;
  }

  @Override
  public List<Contract> getContractsByDateDao(String startDate, String endDate){
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      String sql =
        "SELECT * FROM CONTRACTS WHERE RENT_BEGINNING BETWEEN ? AND ?";

      st = conn.prepareStatement(sql);
      st.setString(1, startDate);
      st.setString(2, endDate);
      rs = st.executeQuery();

      List<Contract> list = new ArrayList<>();

      while (rs.next()) list.add(instantiateContractDao(rs));

      return list.isEmpty() ? null : list;
    } catch (SQLException e) {
      throw new DbException("Erro ao buscar os contratos: " + e.getMessage());
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }
  }

  @Override
  public List<Contract> searchDao(String filter, String argument) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      int paramIndex = 1;
      StringBuilder sql = new StringBuilder("SELECT C.* FROM CONTRACTS C");

      if (
        filter != null && !filter.isEmpty() &&
        argument != null && !argument.isEmpty()
      ) {
        switch (filter) {
          case "Contrato" -> sql.append(" WHERE C.ID LIKE ?");
          case "Locatário" -> sql.append(
            " WHERE C.TENANT_ID IN (SELECT ID FROM CLIENTS WHERE NAME " +
            "LIKE ? AND CLIENT_TYPE = 'TENANT')"
          );
          case "Locador" -> sql.append(
            " WHERE C.LANDLORD_ID IN (SELECT ID FROM CLIENTS WHERE NAME " +
            "LIKE ? AND CLIENT_TYPE = 'LANDLORD')"
          );
          case "Valor do aluguel" -> sql.append(" WHERE C.RENT_VALUE LIKE ?");
          default -> throw new DbException("Filtro inválido: " + filter);
        }
      }

      st = conn.prepareStatement(sql.toString());

      if (argument != null && !argument.isEmpty()) {
        String searchPattern = "%" + argument + "%";
        st.setString(paramIndex++, searchPattern);
      }
      rs = st.executeQuery();

      List<Contract> list = new ArrayList<>();

      while (rs.next()) list.add(instantiateContractDao(rs));

      return list.isEmpty() ? null : list;
    } catch (SQLException e) {
      throw new DbException("Error executing search: " + e.getMessage());
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }
  }

  @Override
  public int countDao() {
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT COUNT(*) FROM CONTRACTS"
    );
      ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
    return 0;
  }

  @Override
  public String getClientDao(int id) {
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT NAME FROM CLIENTS WHERE ID = ?"
    )) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next())
          return rs.getString(1);
      }
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
    return null;
  }

  @Override
  public List<String> getGuarantorsDao(int id) throws DbException {
    String sql = """
      SELECT G.GUARANTOR_NAME AS GUARANTOR_NAME, G.PARTNER_NAME,
      G.IS_SPONSOR_MARRIED
      FROM GUARANTORS G
      JOIN CONTRACTS C ON C.GUARANTOR_ID = G.ID
      WHERE C.ID = ?
    """;

    List<String> guarantors = new ArrayList<>();

    try (PreparedStatement st = conn.prepareStatement(sql)) {
      st.setInt(1, id);

      try (ResultSet rs = st.executeQuery()) {
        while (rs.next()) {
          String guarantorName = rs.getString("GUARANTOR_NAME");
          if (guarantorName != null && !guarantorName.isBlank())
            guarantors.add(guarantorName);

          if (rs.getBoolean("IS_SPONSOR_MARRIED")) {
            String partnerName = rs.getString("PARTNER_NAME");
            if (partnerName != null && !partnerName.isBlank()) {
              guarantors.add(partnerName);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new DbException("Erro ao buscar fiador para o ID: " + id, e);
    }

    return guarantors;
  }
}
