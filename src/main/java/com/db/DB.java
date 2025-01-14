package com.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DB {
  private static Connection conn = null;

  private static void createTables(Statement stmt) {
    try {
      String createClientsTableSql = """
        CREATE TABLE IF NOT EXISTS `CLIENTS` (
          `ID` INT NOT NULL AUTO_INCREMENT,
          `NAME` VARCHAR(255) NOT NULL,
          `CPF_CNPJ` VARCHAR(20) NOT NULL,
          `RG` VARCHAR(20) DEFAULT NULL,
          `ISSUING_ORGANIZATION` VARCHAR(20) DEFAULT NULL,
          `BIRTH_DATE` DATE NOT NULL,
          `TELEPHONE` VARCHAR(50) NOT NULL,
          `CLIENT_TYPE` ENUM('TENANT','LANDLORD') NOT NULL,
          `MARITAL_STATUS` ENUM('SINGLE','MARRIED','DIVORCED','WIDOWED')
          DEFAULT NULL,
          `ADDRESS` VARCHAR(255) DEFAULT NULL,
          `NATIONALITY` VARCHAR(50) DEFAULT NULL,
          `PROFESSION` VARCHAR(50) DEFAULT NULL,
          `NEIGHBORHOOD` VARCHAR(255) DEFAULT NULL,
          `CITY` VARCHAR(255) DEFAULT NULL,
          `STATE` ENUM('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT',
          'MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP',
          'SE','TO') NULL,
          `ZIP` VARCHAR(8) DEFAULT NULL,
          PRIMARY KEY (`ID`)
        ) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_0900_AI_CI;
      """;
      stmt.executeUpdate(createClientsTableSql);

      String createGuarantorsTableSql = """
        CREATE TABLE IF NOT EXISTS `GUARANTORS` (
          `ID` INT NOT NULL AUTO_INCREMENT,
          `TENANT_ID` INT DEFAULT NULL,
          `GUARANTOR_NAME` VARCHAR(255) NOT NULL,
          `PARTNER_NAME` VARCHAR(255) DEFAULT NULL,
          `IS_SPONSOR_MARRIED` TINYINT(1) NOT NULL,
          PRIMARY KEY (`ID`)
        ) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_0900_AI_CI;
      """;
      stmt.executeUpdate(createGuarantorsTableSql);

      String createEstatesTableSql = """
        CREATE TABLE IF NOT EXISTS `ESTATES` (
          `ID` INT NOT NULL AUTO_INCREMENT,
          `ADDRESS` VARCHAR(255) NOT NULL,
          `NUMBER` INT DEFAULT NULL,
          `NEIGHBORHOOD` VARCHAR(100) DEFAULT NULL,
          `CITY` VARCHAR(100) DEFAULT NULL,
          `STATE` ENUM('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT',
          'MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP',
          'SE','TO') DEFAULT NULL,
          `TENANT_ID` INT DEFAULT NULL,
          `LANDLORD_ID` INT DEFAULT NULL,
          `DESCRIPTION` TEXT,
          PRIMARY KEY (`ID`)
        ) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_0900_AI_CI;
      """;
      stmt.executeUpdate(createEstatesTableSql);

      String createContractsTableSql = """
        CREATE TABLE IF NOT EXISTS `CONTRACTS` (
          `ID` INT NOT NULL AUTO_INCREMENT,
          `TENANT_ID` INT DEFAULT NULL,
          `LANDLORD_ID` INT DEFAULT NULL,
          `ESTATE_ID` INT DEFAULT NULL,
          `FILE_BASE64` TEXT,
          `RENT_BEGINNING` DATE DEFAULT NULL,
          `RENT_END` DATE DEFAULT NULL,
          `GUARANTEE_TYPE` ENUM(
          'DEPOSIT','GUARANTOR','CAPITALIZATION_TITLE','BAIL_INSURANCE'
          ) DEFAULT NULL,
          `GUARANTOR_ID` INT DEFAULT NULL,
          `DEPOSIT` DECIMAL(10,2) DEFAULT NULL,
          `RENT_VALUE` DECIMAL(10,2) DEFAULT NULL,
          `ENERGY_CONSUMER_UNIT` VARCHAR(20) DEFAULT NULL,
          `WATER_REGISTRATION_NUMBER` VARCHAR(10) DEFAULT NULL,
          `CONTRACT_SIGNING_DATE` DATE DEFAULT NULL,
          PRIMARY KEY (`ID`),
          KEY `CLIENT_ID` (`GUARANTOR_ID`),
          KEY `CONTRACTS_IBFK_1` (`TENANT_ID`),
          KEY `CONTRACTS_IBFK_2` (`LANDLORD_ID`),
          CONSTRAINT `CONTRACTS_IBFK_1`
          FOREIGN KEY (`TENANT_ID`) REFERENCES `CLIENTS` (`ID`),
          CONSTRAINT `CONTRACTS_IBFK_2`
          FOREIGN KEY (`LANDLORD_ID`) REFERENCES `CLIENTS` (`ID`),
          CONSTRAINT `CONTRACTS_CHK_1` CHECK ((`TENANT_ID` <> `LANDLORD_ID`))
        ) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_0900_AI_CI;
      """;
      stmt.executeUpdate(createContractsTableSql);
    } catch (SQLException e) {
      System.out.println("Error while creating tables: " + e.getMessage());
    }
  }

  private static void createDatabaseIfNotExists(String dbName, Statement stmt) {
    String sql = String.format("CREATE DATABASE IF NOT EXISTS %s", dbName);
    try {
      stmt.executeUpdate(sql);
    } catch (SQLException e) {
      throw new DbException("Error creating database: " + e.getMessage(), e);
    }
  }

  private static boolean doTablesExist(Statement stmt) throws SQLException {
    String checkTableSql = "SHOW TABLES LIKE 'CLIENTS'";
    try (ResultSet rs = stmt.executeQuery(checkTableSql)) {
      return rs.next();
    }
  }

  private static boolean doesDatabaseExist(Connection baseConn, String dbName) {
    String query =
    "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
    try (PreparedStatement pstmt = baseConn.prepareStatement(query)) {
      pstmt.setString(1, dbName);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next(); // Return true if a result is found
      }
    } catch (SQLException e) {
      throw new DbException(e.getMessage());
    }
  }

  private static Connection createBaseConnection(
    String baseUrl, Properties props
  ) throws SQLException {
    return DriverManager.getConnection(baseUrl, props);
  }

  private static void createMainConnection(
    String fullUrl, Properties props
  ) throws SQLException {
    conn = DriverManager.getConnection(fullUrl, props);
  }

  public static Connection getConnection() {
    if (conn == null) {
      try {
        Properties props = loadProperties();
        String fullUrl = props.getProperty("dburl");
        if (fullUrl == null || fullUrl.isEmpty())
          throw new DbException("DB URL is missing in the properties file.");

        String baseUrl = fullUrl.substring(0, fullUrl.lastIndexOf('/'));
        String dbName = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);

        // Create the base connection to the MySQL server
        try (
          Connection baseConn = createBaseConnection(baseUrl, props);
          Statement stmt = baseConn.createStatement()
        ) {
          createDatabaseIfNotExists(dbName, stmt);

          if (!doesDatabaseExist(baseConn, dbName))
            throw new DbException("Database " + dbName + " not found.");

          // Create the actual DB connection
          try (
            Connection dbConn = DriverManager.getConnection(fullUrl, props);
            Statement dbStmt = dbConn.createStatement()) {
            if (!doTablesExist(dbStmt)) {
              createTables(dbStmt);
            }
          }
        }
        // Establish the main connection after ensuring the database exists
        createMainConnection(fullUrl, props);
      } catch (SQLException e) {
        throw new DbException("Failed to connect to the DB: " + e.getMessage());
      }
    }
    return conn;
  }

  public static void closeConnection() {
    if (conn != null)
      try {
        conn.close();
      } catch (SQLException e) {
        throw new DbException(e.getMessage());
      }
  }

  private static Properties loadProperties() {
    try (FileInputStream fs = new FileInputStream(
      "src/main/java/com/db.properties")
    ) {
      Properties props = new Properties();
      props.load(fs);
      return props;
    } catch (IOException e) {
      throw new DbException(e.getMessage());
    }
  }

  public static void closeStatement(Statement st) {
    if (st != null)
      try {
        st.close();
      } catch (SQLException e) {
        throw new DbException(e.getMessage());
      }
  }

  public static void closeResultSet(ResultSet rs) {
    if (rs != null)
      try {
        rs.close();
      } catch (SQLException e) {
        throw new DbException(e.getMessage());
      }
  }
}
