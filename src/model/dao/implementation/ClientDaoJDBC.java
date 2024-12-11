package model.dao.implementation;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.InputMismatchException;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import model.entities.Guarantee;
import model.entities.Client;
import model.dao.ClientDao;
import java.time.LocalDate;
import db.DbException;
import db.DB;

public class ClientDaoJDBC implements ClientDao {
  private Connection conn;

  public ClientDaoJDBC(Connection conn) {
    this.conn = conn;
  }

  @Override
  public void insertDao(Client obj) {
    PreparedStatement st = null;
    try {
      String name = obj.getName();
      String cpfCnpj = obj.getCpfCnpj();
      String telephone = obj.getTelephone();
      Date birthDate =
      obj.getBirthDate() != null ? Date.valueOf(obj.getBirthDate()) : null;
      String clientType = obj.getClientType().name();

      st = conn.prepareStatement(
        "INSERT INTO CLIENTS (" +
        "NAME, CPF_CNPJ, TELEPHONE, BIRTH_DATE, CLIENT_TYPE) " +
        "VALUES (?, ?, ?, ?, ?)"
      );

      st.setString(1, name);
      st.setString(2, cpfCnpj);
      st.setString(3, telephone);
      st.setDate(4, birthDate);
      st.setString(5, clientType);

      int rowsAffected = st.executeUpdate();
      if (rowsAffected == 0)
        throw new DbException("Unexpected error! No rows affected.");
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    } finally {
      DB.closeStatement(st);
    }
  }

  @Override
  public void updateDao(Client obj) {
    PreparedStatement st = null;
    try {
      st = conn.prepareStatement(
        "UPDATE CLIENTS " +
        "SET NAME = ?, TELEPHONE = ? WHERE ID = ?"
      );

      st.setString(1, obj.getName());
      st.setString(2, obj.getTelephone());
      st.setInt(3, obj.getId());

      st.executeUpdate();
    } catch (SQLException e) {
      throw new DbException("Error updating client with ID: " + obj.getId(), e);
    } finally {
      DB.closeStatement(st);
    }
  }

  @Override
  public void deleteByIdDao(int id) {
    PreparedStatement st = null;
    try {
      st = conn.prepareStatement("DELETE FROM CLIENTS WHERE ID = ?");
      st.setInt(1, id);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    } finally {
      DB.closeStatement(st);
    }
  }

  public int countDao() {
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT COUNT(*) FROM CLIENTS"
    );
      ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
    return 0;
  }

  @Override
  public List<Client> searchDao(String filter, String argument) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {      
      int paramIndex = 1;
      StringBuilder sql = new StringBuilder("SELECT * FROM CLIENTS c");

      if (filter != null) filter = filter.toLowerCase();

      if (filter != null && !filter.isEmpty() && argument != null
          && !argument.isEmpty()) {
        switch (filter) {
          case "nome":
            sql.append(" WHERE c.name LIKE ?");
            break;
          case "cpf | cnpj":
            sql.append(" WHERE c.cpf_cnpj LIKE ?");
            break;
          case "telefone":
            sql.append(" WHERE c.telephone LIKE ?");
            break;
          case "contrato":
            sql.append(" JOIN CLIENT_CONTRACT cc ON cc.CLIENT_ID = c.id ");
            sql.append(" JOIN CONTRACTS con ON con.ID = cc.CONTRACT_ID ");
            sql.append(" WHERE con.ID LIKE ?");
            break;
          case "tipo":
            sql.append(" WHERE c.client_type LIKE ?");
            argument = mapClientTypeToDb(argument);
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

      List<Client> list = new ArrayList<>();
      // Convert ResultSet to Client object
      while (rs.next()) list.add(instantiateClientDao(rs));

      return list.isEmpty() ? null : list;
    } catch (SQLException e) {
      throw new DbException("Error executing search: " + e.getMessage());
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }
  }

  public String mapClientTypeToDb(String clientType) {
    try {
      switch (clientType) {
        case "locador":
          return "LANDLORD";
        case "locatario":
        case "locatário":
          return "TENANT";
        default:
          throw new DbException("Tipo " + clientType + " é inválido");
      }
    } catch (InputMismatchException e) {
      return null;
    }
  }

  public List<Client> findPaginatedDao(int page, int pageSize) {
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT * FROM CLIENTS LIMIT ? OFFSET ?"
    )) {
      ps.setInt(1, pageSize);
      ps.setInt(2, (page - 1) * pageSize);

      List<Client> CLIENTS = new ArrayList<>();
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        Client client = instantiateClientDao(rs);
        CLIENTS.add(client);
      }
      return CLIENTS;

    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
  }

  public List<String> getContractsByCpfCnpjDao(String cpfCnpj)
    throws DbException
  {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<String> contracts = new ArrayList<>();

    try {
      st = conn.prepareStatement(
        "SELECT CONTRACT_ID FROM CLIENT_CONTRACT " +
        "JOIN CLIENTS ON CLIENTS.ID = CLIENT_CONTRACT.CLIENT_ID " +
        "WHERE CLIENTS.CPF_CNPJ = ?"
      );
      st.setString(1, cpfCnpj);
      rs = st.executeQuery();

      while (rs.next()) {
        String contractId = rs.getString("contract_id");
        contracts.add(contractId);
      }

    } catch (SQLException e) {
      throw new DbException(
        "Erro ao buscar contratos para o CPF/CNPJ: " + cpfCnpj, e);
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }
    return contracts;
  }

  public List<String> getGuarantorsByCpfCnpjDao(String cpfCnpj)
    throws DbException
  {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<String> guarantors = new ArrayList<>();

    try {
      st = conn.prepareStatement(
        "SELECT CLIENT_GUARANTOR.GUARANTOR_NAME, " +
        "GUARANTORS.PARTNER_NAME, GUARANTORS.IS_SPONSOR_MARRIED " +
        "FROM CLIENT_GUARANTOR " +
        "JOIN CLIENTS ON CLIENTS.ID = CLIENT_GUARANTOR.CLIENT_ID " +
        "JOIN GUARANTORS ON CLIENT_GUARANTOR.GUARANTOR_NAME = " +
        "GUARANTORS.NAME " +
        "WHERE CLIENTS.CPF_CNPJ = ?"
      );
      st.setString(1, cpfCnpj);
      rs = st.executeQuery();

      while (rs.next()) {
        String guarantorName = rs.getString("GUARANTOR_NAME");
        String partnerName = rs.getString("PARTNER_NAME");
        boolean isMarried = rs.getBoolean("IS_SPONSOR_MARRIED");

        guarantors.add(guarantorName);
        if (isMarried && partnerName != null && !partnerName.isEmpty())
          guarantors.add(partnerName);
      }

    } catch (SQLException e) {
      throw new DbException(
        "Error retrieving guarantors for CPF/CNPJ: " + cpfCnpj, e
      );
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }

    return guarantors;
  }

  private Client instantiateClientDao(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("NAME");
    String cpfCnpj = rs.getString("CPF_CNPJ");
    LocalDate birthDate = rs.getDate("BIRTH_DATE").toLocalDate();
    String telephone = rs.getString("TELEPHONE");

    List<Integer> contractIds = new ArrayList<>();

    try (PreparedStatement contractStmt = conn.prepareStatement(
      "SELECT CONTRACT_ID FROM CLIENT_CONTRACT WHERE CLIENT_ID = ?"
    )){
      contractStmt.setInt(1, id);
      ResultSet contractRs = contractStmt.executeQuery();

      while (contractRs.next())
        contractIds.add(contractRs.getInt("CONTRACT_ID"));
    }

    List<String> contracts = new ArrayList<>();
    if (!contractIds.isEmpty()) {
      contracts = contractIds.stream()
      .map(String::valueOf)
      .collect(Collectors.toList());
    }

    String guarantorStr = rs.getString("GUARANTORS");

    List<String> guarantor = new ArrayList<>();
    if (guarantorStr != null && !guarantorStr.isEmpty())
      guarantor = Arrays.asList(guarantorStr.split(","));

    double deposit = rs.getDouble("DEPOSIT");

    // Convert clientType string to ClientType enum
    String clientTypeStr = rs.getString("CLIENT_TYPE");
    Client.ClientType clientType =
    Client.ClientType.valueOf(clientTypeStr.toUpperCase());

    String guaranteeTypeStr = rs.getString("GUARANTEE_TYPE");
    Guarantee.GuaranteeType guaranteeType = null;

    if (guaranteeTypeStr != null && !guaranteeTypeStr.isEmpty()) {
      try {
        guaranteeType =
        Guarantee.GuaranteeType.valueOf(guaranteeTypeStr.toUpperCase());
      } catch (IllegalArgumentException e) {
        System.out.println("Invalid GuaranteeType found: " + guaranteeTypeStr);
      }
    }

    return new Client(
      id, name, cpfCnpj, birthDate, contracts, telephone,
      guaranteeType, guarantor, deposit, clientType
    );
  }
}
