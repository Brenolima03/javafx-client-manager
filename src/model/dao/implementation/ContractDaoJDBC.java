package model.dao.implementation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import db.DB;
import db.DbException;
import model.dao.ContractDao;
import model.entities.Contract;
import model.entities.Guarantee;

public class ContractDaoJDBC implements ContractDao {
  private Connection conn;

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
      String fileBase64 = obj.getFileBase64();
      Date rentBeginning = obj.getRentBeginning() != null ? 
        Date.valueOf(obj.getRentBeginning()) : null;
      Date rentEnd = obj.getRentEnd() != null ? 
        Date.valueOf(obj.getRentEnd()) : null;
      double rentValue = obj.getRentValue();
      double deposit = obj.getDepositValue();
      double waterBill = obj.getWaterBill();
      double energyBill = obj.getEnergyBill();
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

      String query = """
        INSERT INTO CONTRACTS (
          TENANT_ID, LANDLORD_ID, FILE_BASE64, RENT_BEGINNING, RENT_END,
          RENT_VALUE, WATER_BILL, ENERGY_BILL, GUARANTOR_ID,
          CONTRACT_SIGNING_DATE, DEPOSIT, GUARANTEE_TYPE
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

      st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

      st.setInt(1, tenantId);
      st.setInt(2, landlordId);
      st.setString(3, fileBase64);
      st.setDate(4, rentBeginning);
      st.setDate(5, rentEnd);
      st.setDouble(6, rentValue);
      st.setDouble(7, waterBill);
      st.setDouble(8, energyBill);
      st.setInt(9, guarantorId);
      st.setDate(10, signingDate != null ? Date.valueOf(signingDate) : null);
      st.setDouble(11, deposit);
      st.setString(12, guaranteeType);

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

    } catch (SQLException e) {
      throw new DbException(
        "Error during contract insertion: " + e.getMessage(), e
      );
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(generatedKeys);
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
      boolean isMarried = !partnerName.trim().isEmpty();

      st = conn.prepareStatement(
        "INSERT INTO GUARANTORS (TENANT_ID, GUARANTOR_NAME, PARTNER_NAME, " +
        "IS_SPONSOR_MARRIED) VALUES (?, ?, ?, ?)",
        Statement.RETURN_GENERATED_KEYS
      );

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

  private Contract instantiateContractDao(ResultSet rs) throws SQLException {
    Contract contract = new Contract();
    try {
      contract.setId(rs.getInt("ID"));
      contract.setTenant(rs.getInt("TENANT_ID"));
      contract.setLandlord(rs.getInt("LANDLORD_ID"));
      contract.setRentBeginning(rs.getDate("RENT_BEGINNING").toLocalDate());
      contract.setRentEnd(rs.getDate("RENT_END").toLocalDate());
      contract.setRentValue(rs.getDouble("RENT_VALUE"));

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
    } catch (Exception e) {
      // Ignore exceptions and continue
      System.err.println(
        "Error while instantiating contract: " + e.getMessage()
      );
    }

    return contract;
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
          case "Contrato":
            sql.append(" WHERE C.ID LIKE ?");
            break;
          case "Locatário":
            sql.append(
              " WHERE C.TENANT_ID IN (SELECT ID FROM CLIENTS WHERE NAME " +
              "LIKE ? AND CLIENT_TYPE = 'TENANT')"
            );
            break;
          case "Locador":
            sql.append(
              " WHERE C.LANDLORD_ID IN (SELECT ID FROM CLIENTS WHERE NAME " +
              "LIKE ? AND CLIENT_TYPE = 'LANDLORD')"
            );
            break;
          case "Valor do aluguel":
            sql.append(" WHERE C.RENT_VALUE LIKE ?");
            break;
          default:
            throw new DbException("Invalid filter selected: " + filter);
        }
      }

      st = conn.prepareStatement(sql.toString());

      if (argument != null && !argument.isEmpty()) {
        String searchPattern = "%" + argument + "%";
        st.setString(paramIndex++, searchPattern);
      }
      rs = st.executeQuery();
  
      List<Contract> list = new ArrayList<>();
      // Use the new method to instantiate Contract objects
      while (rs.next()) list.add(instantiateContractDao(rs));
  
      return list.isEmpty() ? null : list;
    } catch (SQLException e) {
      throw new DbException("Error executing search: " + e.getMessage());
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }
  }

  public List<Contract> findPaginatedDao(int page, int pageSize) {
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT * FROM CONTRACTS LIMIT ? OFFSET ?"
    )) {
      ps.setInt(1, pageSize);
      ps.setInt(2, (page - 1) * pageSize);

      List<Contract> contracts = new ArrayList<>();
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        contracts.add(instantiateContractDao(rs));
      }

      return contracts;

    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
  }

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
}
