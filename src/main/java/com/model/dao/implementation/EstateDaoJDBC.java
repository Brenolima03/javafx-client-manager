package com.model.dao.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.db.DB;
import com.db.DbException;
import com.model.dao.EstateDao;
import com.model.entities.Estate;

public class EstateDaoJDBC implements EstateDao {
  private Connection conn;

  public EstateDaoJDBC(Connection conn) {
    this.conn = conn;
  }

  @Override
  public void insertDao(Estate obj) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      String sql = """
        INSERT INTO ESTATES
          (ADDRESS, NUMBER, NEIGHBORHOOD, CITY, STATE, LANDLORD_ID, DESCRIPTION)
        VALUES (?, ?, ?, ?, ?, ?, ?)
      """;
      st = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

      st.setString(1, obj.getAddress());
      st.setInt(2, obj.getNumber());
      st.setString(3, obj.getNeighborhood());
      st.setString(4, obj.getCity());
      st.setString(5, obj.getState());
      st.setInt(6, obj.getLandlordId());
      st.setString(7, obj.getDescription());

      int rowsAffected = st.executeUpdate();

      if (rowsAffected > 0) {
        rs = st.getGeneratedKeys();
        if (rs.next()) {
          int id = rs.getInt(1);
          obj.setId(id);
        }
      } else {
        throw new DbException("Unexpected error! No rows affected.");
      }
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    } finally {
      DB.closeResultSet(rs);
      DB.closeStatement(st);
    }
  }

  @Override
  public Estate findStateDao(int id) {
    String sql = """
      SELECT ID, TENANT_ID, LANDLORD_ID, ADDRESS, NUMBER, NEIGHBORHOOD, CITY,
      STATE, DESCRIPTION FROM ESTATES WHERE ID = ?        
    """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        int estateId = rs.getInt("ID");
        int tenantId = rs.getInt("TENANT_ID");
        int landlordId = rs.getInt("LANDLORD_ID");
        String address = rs.getString("ADDRESS");
        int number = rs.getInt("NUMBER");
        String neighborhood = rs.getString("NEIGHBORHOOD");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String description = rs.getString("DESCRIPTION");

        return new Estate(
          estateId, tenantId, landlordId, address, number,
          neighborhood, city, state, description
        );
      }
      return null;
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
  }

  @Override
  public List<Estate> findAllEstatesDao() {
    List<Estate> estates = new ArrayList<>();

    try (
      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ESTATES");
      ResultSet rs = stmt.executeQuery()
    ) {

      while (rs.next()) {
        Estate estate = instantiateEstateDao(rs);
        estates.add(estate);
      }
    } catch (SQLException e) {
    }

    return estates;
  }

  @Override
  public void updateDao(
    int estateId, LinkedHashMap<String, Object> fieldsUpdated
  ) {
    if (fieldsUpdated == null || fieldsUpdated.isEmpty())
      throw new IllegalArgumentException("No fields provided for update.");

    StringBuilder sql = new StringBuilder("UPDATE ESTATES SET ");
    int i = 0;

    for (String field : fieldsUpdated.keySet()) {
      switch (field) {
        case "description":
          sql.append("DESCRIPTION = ?");
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

      st.setInt(i, estateId);
      int rowsAffected = st.executeUpdate();

      if (rowsAffected == 0)
        throw new DbException("No client found with ID: " + estateId);
    } catch (SQLException e) {
      throw new DbException("Error updating client with ID: " + estateId, e);
    }
  }

  private Estate instantiateEstateDao(ResultSet rs) throws SQLException {
    Estate estate = new Estate();
    try {
      estate.setId(rs.getInt("ID"));
      estate.setAddress(rs.getString("ADDRESS"));
      estate.setNumber(rs.getInt("NUMBER"));
      estate.setNeighborhood(rs.getString("NEIGHBORHOOD"));
      estate.setCity(rs.getString("CITY"));
      estate.setState(rs.getString("STATE"));
      estate.setTenantId(rs.getInt("TENANT_ID"));
      estate.setLandlordId(rs.getInt("LANDLORD_ID"));
      estate.setDescription(rs.getString("DESCRIPTION"));
    } catch (SQLException e) {
      System.err.println("Error while instantiating estate: " + e.getMessage());
    }
    return estate;
  }

  @Override
  public List<Estate> searchDao(String filter, String argument) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      int paramIndex = 1;
      StringBuilder sql = new StringBuilder("SELECT E.* FROM ESTATES E");

      if (
        filter != null && !filter.isEmpty() &&
        argument != null && !argument.isEmpty()
      ) {
        switch (filter) {
          case "Endereço" -> sql.append(" WHERE E.ADDRESS LIKE ?");
          case "Bairro" -> sql.append(" WHERE E.NEIGHBORHOOD LIKE ?");
          case "Locador" -> sql.append(
              " WHERE E.LANDLORD_ID IN (SELECT ID FROM CLIENTS WHERE NAME " +
              "LIKE ? AND CLIENT_TYPE = 'LANDLORD')"
            );
          case "Valor do aluguel" -> sql.append(
            " WHERE E.ID IN (SELECT ESTATE_ID FROM CONTRACTS " +
            "WHERE RENT_VALUE LIKE ?)");
          default -> throw new DbException("Filtro inválido: " + filter);
        }
      }

      st = conn.prepareStatement(sql.toString());

      if (argument != null && !argument.isEmpty()) {
        String searchPattern = "%" + argument + "%";
        st.setString(paramIndex++, searchPattern);
      }
      rs = st.executeQuery();

      List<Estate> list = new ArrayList<>();
      // Use the new method to instantiate Estate objects
      while (rs.next()) list.add(instantiateEstateDao(rs));

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
      "SELECT COUNT(*) FROM ESTATES"
    );
      ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
    return 0;
  }

  @Override
  public List<Estate> getAllClientEstatesDao(int landlordId) {
    try (PreparedStatement ps = conn.prepareStatement(
      "SELECT * FROM ESTATES WHERE LANDLORD_ID = ?"
    )) {
      ps.setInt(1, landlordId);

      List<Estate> estates = new ArrayList<>();
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        Estate estate = instantiateEstateDao(rs);
        estates.add(estate);
      }
      return estates;

    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
  }
}
