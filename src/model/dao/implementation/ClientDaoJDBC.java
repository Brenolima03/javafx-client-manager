package model.dao.implementation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import db.DB;
import db.DbException;
import model.dao.ClientDao;
import model.entities.Client;
import model.entities.Client.ClientType;

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
      String rg = obj.getRg();
      String issuingOrganization = obj.getIssuingOrganization();
      String telephone = obj.getTelephone();
      Date birthDate = obj.getBirthDate() != null ?
        Date.valueOf(obj.getBirthDate()) : null;
      String clientType = obj.getClientType().name();
      boolean isMarried = obj.isMarried();
      String address = obj.getAddress();
      String nationality = obj.getNationality();
      String profession = obj.getProfession();
      String neighborhood = obj.getNeighborhood();
      String city = obj.getCity();
      String state = obj.getState();
      String zip = obj.getZip();

      st = conn.prepareStatement(
        "INSERT INTO CLIENTS (" +
        "NAME, CPF_CNPJ, RG, ISSUING_ORGANIZATION, TELEPHONE, BIRTH_DATE, " +
        "CLIENT_TYPE, IS_MARRIED, ADDRESS, NATIONALITY, PROFESSION, " +
        "NEIGHBORHOOD, CITY, STATE, CEP" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      );
      st.setString(1, name);
      st.setString(2, cpfCnpj);
      st.setString(3, rg);
      st.setString(4, issuingOrganization);
      st.setString(5, telephone);
      st.setDate(6, birthDate);
      st.setString(7, clientType);
      st.setBoolean(8, isMarried);
      st.setString(9, address);
      st.setString(10, nationality);
      st.setString(11, profession);
      st.setString(12, neighborhood);
      st.setString(13, city);
      st.setString(14, state);
      st.setString(15, zip);

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
  public String findClientByIdDao(int id) {
    String clientName = null;
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT NAME FROM CLIENTS WHERE ID = ?"
    )) {
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();

      if (rs.next())
          clientName = rs.getString("NAME");

      return clientName;
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
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
      st = conn.prepareStatement(
        "DELETE FROM CONTRACTS WHERE TENANT_ID = ? OR LANDLORD_ID = ?"
      );
      st.setInt(1, id);
      st.setInt(2, id);
      st.executeUpdate();
      DB.closeStatement(st);

      st = conn.prepareStatement("DELETE FROM GUARANTORS WHERE TENANT_ID = ?");
      st.setInt(1, id);
      st.executeUpdate();
      DB.closeStatement(st);

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
  public List<Client> findAllDao() {
    List<Client> clients = new ArrayList<>();
    String query = "SELECT * FROM CLIENTS";

    try (
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet rs = stmt.executeQuery()
    ) {

      while (rs.next()) {
        Client client = new Client();
        client.setId(rs.getInt("ID"));
        client.setName(rs.getString("NAME"));
        client.setCpfCnpj(rs.getString("CPF_CNPJ"));
        client.setClientType(ClientType.valueOf(rs.getString("CLIENT_TYPE")));
        clients.add(client);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return clients;
  }

  public List<String> findStatesDao() {
    List<String> states = new ArrayList<>();

    String query = """
      SELECT COLUMN_TYPE
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_NAME = 'CLIENTS' AND COLUMN_NAME = 'STATE';
    """;

    try (
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet rs = stmt.executeQuery()
    ) {
      if (rs.next()) {
        String enumValues = rs.getString("COLUMN_TYPE");
        String enumString = enumValues.substring(
          enumValues.indexOf('(') + 1, enumValues.indexOf(')')
        );
        String[] statesArray = enumString.split(",");

        for (String state : statesArray)
          states.add(state.trim().replace("'", ""));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return states;
  }

  @Override
  public List<Client> searchDao(String filter, String argument) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {      
      int paramIndex = 1;
      StringBuilder sql =
        new StringBuilder("SELECT DISTINCT C.* FROM CLIENTS c");

      if (filter != null && !filter.isEmpty() && argument != null
          && !argument.isEmpty()) {
        switch (filter) {
          case "Nome":
            sql.append(" WHERE C.NAME LIKE ?");
            break;
          case "CPF | CNPJ":
            sql.append(" WHERE C.CPF_CNPJ LIKE ?");
            break;
          case "Telefone":
            sql.append(" WHERE C.TELEPHONE LIKE ?");
            break;
          case "Contrato":
            sql.append(" JOIN CONTRACTS CT ON C.ID = CT.TENANT_ID OR " +
            "C.ID = CT.LANDLORD_ID WHERE CT.ID LIKE ?");
            break;
          case "Tipo":
            sql.append(" WHERE C.CLIENT_TYPE LIKE ?");
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
    Map<String, String> clientTypeMap = Map.of(
      "Locador", "LANDLORD",
      "locador", "LANDLORD",
      "Locatario", "TENANT",
      "locatario", "TENANT",
      "Locatário", "TENANT",
      "locatário", "TENANT"
    );
    
    String result = clientTypeMap.get(clientType);
    if (result == null) {
      throw new DbException("Tipo " + clientType + " é inválido");
    }
    return result;
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

  public List<String> getGuarantorsById(int id) throws DbException {
    String query = """
      SELECT G.GUARANTOR_NAME AS GUARANTOR_NAME, G.PARTNER_NAME,
      G.IS_SPONSOR_MARRIED
      FROM GUARANTORS G
      JOIN CONTRACTS C ON C.GUARANTOR_ID = G.ID
      WHERE C.ID = ?
    """;

    List<String> guarantors = new ArrayList<>();

    try (PreparedStatement st = conn.prepareStatement(query)) {
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
      throw new DbException("Error retrieving guarantors for ID: " + id, e);
    }

    return guarantors;
  }

  public String getGuaranteeTypeByContractIdDao(int id) {
    String sql = """
      SELECT C.GUARANTEE_TYPE
      FROM CONTRACTS C
      JOIN CLIENTS CL ON CL.ID = C.TENANT_ID
      WHERE C.ID = ?
    """;
    
    String guaranteeType = null;

    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next())
          guaranteeType = resultSet.getString("GUARANTEE_TYPE");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return guaranteeType;
  }

  public double getDeposit(int contract) {
    String sql = """
      SELECT C.DEPOSIT 
      FROM CONTRACTS C 
      JOIN CLIENTS CL ON CL.ID = C.TENANT_ID
      WHERE C.ID = ?
    """;

    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, contract);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) return resultSet.getDouble("DEPOSIT");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private Client instantiateClientDao(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("NAME");
    String cpfCnpj = rs.getString("CPF_CNPJ");
    LocalDate birthDate = rs.getDate("BIRTH_DATE") != null 
      ? rs.getDate("BIRTH_DATE").toLocalDate() 
      : null;
    String telephone = rs.getString("TELEPHONE");

    String clientTypeStr = rs.getString("CLIENT_TYPE");
    Client.ClientType clientType =
      Client.ClientType.valueOf(clientTypeStr.toUpperCase());

    boolean isMarried = false;
    String address = null;
    String nationality = null;
    String profession = null;
    String neighborhood = null;
    String city = null;
    String state = null;
    String zip = null;
    int contractID = getContractIdByClientId(id);

    return new Client(
      id, name, cpfCnpj, null, null, 
      birthDate, contractID, telephone, 
      clientType, isMarried, address,
      nationality, profession, neighborhood, 
      city, state, zip
    );
  }

  private int getContractIdByClientId(int clientId) throws SQLException {
    PreparedStatement st = null;
    ResultSet rs = null;
    int contractId = 0;

    try {
      st = conn.prepareStatement(
        "SELECT ID FROM CONTRACTS " +
        "WHERE TENANT_ID = ? OR LANDLORD_ID = ?"
      );
      st.setInt(1, clientId);
      st.setInt(2, clientId);
      rs = st.executeQuery();

      if (rs.next()) {
        contractId = rs.getInt("ID");
      }
    } catch (SQLException e) {
      throw new SQLException(
        "Error retrieving contract ID for client ID: " + clientId, e
      );
    } finally {
      DB.closeStatement(st);
      DB.closeResultSet(rs);
    }
    return contractId;
  }
}
