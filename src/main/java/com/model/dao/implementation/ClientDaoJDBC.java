package com.model.dao.implementation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.db.DB;
import com.db.DbException;
import com.model.dao.ClientDao;
import com.model.entities.Client;
import com.model.entities.Client.ClientType;
import com.model.entities.Client.MaritalStatus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
      String maritalStatus = obj.getMaritalStatus().name();
      String address = obj.getAddress();
      String nationality = obj.getNationality();
      String profession = obj.getProfession();
      String neighborhood = obj.getNeighborhood();
      String city = obj.getCity();
      String state = obj.getState();
      String zip = obj.getZip();

      String sql = """
        INSERT INTO CLIENTS (
        NAME, CPF_CNPJ, RG, ISSUING_ORGANIZATION, TELEPHONE, BIRTH_DATE, 
        CLIENT_TYPE, MARITAL_STATUS, ADDRESS, NATIONALITY, PROFESSION, 
        NEIGHBORHOOD, CITY, STATE, ZIP
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;
      st = conn.prepareStatement(sql);
      st.setString(1, name);
      st.setString(2, cpfCnpj);
      st.setString(3, rg);
      st.setString(4, issuingOrganization);
      st.setString(5, telephone);
      st.setDate(6, birthDate);
      st.setString(7, clientType);
      st.setString(8, maritalStatus);
      st.setString(9, address);
      st.setString(10, nationality);
      st.setString(11, profession);
      st.setString(12, neighborhood);
      st.setString(13, city);
      st.setString(14, state);
      st.setString(15, zip);

      int rowsAffected = st.executeUpdate();
      if (rowsAffected == 0)
        throw new DbException("Erro inesperado, nenhuma ação realizada.");
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    } finally {
      DB.closeStatement(st);
    }
  }

  private List<Integer> getContracts(int clientId) {
    String sql = "SELECT CONTRACT_ID FROM CLIENT_CONTRACT WHERE CLIENT_ID = ?";
    List<Integer> contracts = new ArrayList<>();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, clientId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        contracts.add(rs.getInt("CONTRACT_ID"));
      }
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
    return contracts;
  }

  @Override
  public Client findClientByIdDao(int id) {
    String sql = """
      SELECT ID, NAME, CPF_CNPJ, RG, ISSUING_ORGANIZATION, BIRTH_DATE,
      TELEPHONE, CLIENT_TYPE, MARITAL_STATUS, ADDRESS, NATIONALITY, PROFESSION,
      NEIGHBORHOOD, CITY, STATE, ZIP FROM CLIENTS WHERE ID = ?
    """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        int clientId = rs.getInt("ID");
        String name = rs.getString("NAME");
        String cpfCnpj = rs.getString("CPF_CNPJ");
        String rg = rs.getString("RG");
        String issuingOrganization = rs.getString("ISSUING_ORGANIZATION");
        LocalDate birthDate = rs.getDate("BIRTH_DATE") != null ?
          rs.getDate("BIRTH_DATE").toLocalDate() : null;
        String telephone = rs.getString("TELEPHONE");

        // Enum mapping for clientType
        String clientTypeString = rs.getString("CLIENT_TYPE");
        Client.ClientType clientType = clientTypeString != null ?
          Client.ClientType.valueOf(clientTypeString.toUpperCase()) :
          Client.ClientType.TENANT;

        // Enum mapping for maritalStatus
        String maritalStatusStr = rs.getString("MARITAL_STATUS");
        Client.MaritalStatus maritalStatus = maritalStatusStr != null ?
          Client.MaritalStatus.valueOf(maritalStatusStr.toUpperCase()) : null;

        String address = rs.getString("ADDRESS");
        String nationality = rs.getString("NATIONALITY");
        String profession = rs.getString("PROFESSION");
        String neighborhood = rs.getString("NEIGHBORHOOD");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String zip = rs.getString("ZIP");

        List<Integer> contracts = getContracts(clientId);

        return new Client(
          clientId, name, cpfCnpj, contracts, rg, issuingOrganization,
          birthDate, telephone, clientType, maritalStatus, address, nationality,
          profession, neighborhood, city, state, zip
        );
      }
      return null;
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
  }

  @Override
  public void updateDao(
    int clientId, LinkedHashMap<String, Object> fieldsUpdated
  ) {
    if (fieldsUpdated == null || fieldsUpdated.isEmpty())
      throw new IllegalArgumentException("No fields provided for update.");

    StringBuilder sql = new StringBuilder("UPDATE CLIENTS SET ");
    int i = 0;

    for (String field : fieldsUpdated.keySet()) {
      switch (field) {
        case "telephone":
          sql.append("TELEPHONE = ?");
          break;
        case "maritalStatus":
          sql.append("MARITAL_STATUS = ?");
          break;
        case "address":
          sql.append("ADDRESS = ?");
          break;
        case "profession":
          sql.append("PROFESSION = ?");
          break;
        case "neighborhood":
          sql.append("NEIGHBORHOOD = ?");
          break;
        case "city":
          sql.append("CITY = ?");
          break;
        case "state":
          sql.append("STATE = ?");
          break;
        case "zip":
          sql.append("ZIP = ?");
          break;
        default:
          throw new IllegalArgumentException("Unknown field: " + field);
      }

      if (i < fieldsUpdated.size() - 1) 
        sql.append(", ");

      i++;
    }

    sql.append(" WHERE ID = ?");

    try (PreparedStatement st = conn.prepareStatement(sql.toString())) {
      i = 1;
      for (Object value : fieldsUpdated.values())
        st.setObject(i++, value);

      st.setInt(i, clientId);
      int rowsAffected = st.executeUpdate();

      if (rowsAffected == 0)
        throw new DbException("No client found with ID: " + clientId);
    } catch (SQLException e) {
      throw new DbException("Error updating client with ID: " + clientId, e);
    }
  }

  @Override
  public void deleteByIdDao(int id, boolean isLandlord) {
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

      if (isLandlord) {
        st = conn.prepareStatement("DELETE FROM ESTATES WHERE LANDLORD_ID = ?");
        st.setInt(1, id);
        st.executeUpdate();
        DB.closeStatement(st);
      }

      st = conn.prepareStatement("DELETE FROM CLIENTS WHERE ID = ?");
      st.setInt(1, id);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    } finally {
      DB.closeStatement(st);
    }
  }

  @Override
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

    try (
      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM CLIENTS");
      ResultSet rs = stmt.executeQuery()
    ) {

      while (rs.next()) {
        Client client = instantiateClientDao(rs);
        clients.add(client);
      }
    } catch (SQLException e) {
    }

    return clients;
  }

  @Override
  public ObservableList<Client> searchDao(String filter, String argument) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {      
      int paramIndex = 1;
      StringBuilder sql =
        new StringBuilder("SELECT DISTINCT C.* FROM CLIENTS c");

      if (filter != null && !argument.isEmpty()) {
        switch (filter) {
          case "Nome" -> sql.append(" WHERE C.NAME LIKE ?");
          case "CPF | CNPJ" ->
            sql.append(" WHERE C.CPF_CNPJ LIKE ?");
          case "RG" ->
            sql.append(" WHERE C.RG LIKE ?");
          case "Telefone" ->
            sql.append(" WHERE C.TELEPHONE LIKE ?");
          case "Contrato" -> sql.append(
            " JOIN CONTRACTS CT ON C.ID = CT.TENANT_ID OR " +
            "C.ID = CT.LANDLORD_ID WHERE CT.ID LIKE ?"
          );
          case "Tipo" -> {
            sql.append(" WHERE C.CLIENT_TYPE LIKE ?");
            argument = mapClientTypeToDb(argument);
          }
          default -> throw new DbException(
            "Filtro inválido: " + filter
          );
        }
      }
      st = conn.prepareStatement(sql.toString());
      if (argument != null && !argument.isEmpty()) {
        String searchPattern = "%" + argument + "%";
        st.setString(paramIndex++, searchPattern);
      }

      rs = st.executeQuery();

      ObservableList<Client> list = FXCollections.observableArrayList();
      // Convert ResultSet to Client object
      while (rs.next()) list.add(instantiateClientDao(rs));

      return list.isEmpty() ? null : list;
    } catch (SQLException e) {
      throw new DbException("Erro ao buscar: " + e.getMessage());
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
    if (result == null)
      throw new DbException("Tipo " + clientType + " é inválido");

    return result;
  }

  private Client instantiateClientDao(ResultSet rs) throws SQLException {
    int clientId = rs.getInt("id");
    String name = rs.getString("NAME");
    String cpfCnpj = rs.getString("CPF_CNPJ");
    String rg = rs.getString("RG");
    String issuingOrganization = rs.getString("ISSUING_ORGANIZATION");
    LocalDate birthDate = rs.getDate("BIRTH_DATE") != null 
      ? rs.getDate("BIRTH_DATE").toLocalDate() 
      : null;
    String telephone = rs.getString("TELEPHONE");

    String maritalStatusStr = rs.getString("MARITAL_STATUS");
    MaritalStatus maritalStatus = maritalStatusStr != null
      ? MaritalStatus.valueOf(maritalStatusStr.toUpperCase())
      : null;

    String address = rs.getString("ADDRESS");
    String nationality = rs.getString("NATIONALITY");
    String profession = rs.getString("PROFESSION");
    String neighborhood = rs.getString("NEIGHBORHOOD");
    String city = rs.getString("CITY");
    String state = rs.getString("STATE");
    String zip = rs.getString("ZIP");

    ClientType clientType = null;
    String clientTypeStr = rs.getString("CLIENT_TYPE");    
    clientType = ClientType.valueOf(clientTypeStr);
    List<Integer> contracts = getContracts(clientId);

    // Return the Client object
    return new Client(
      clientId, name, cpfCnpj, contracts, rg, issuingOrganization, 
      birthDate, telephone, 
      clientType, maritalStatus, address,
      nationality, profession, neighborhood, 
      city, state, zip
    );
  }
}
