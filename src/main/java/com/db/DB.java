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

      String seedClientsSql = """
        INSERT INTO `CLIENTS` (`NAME`, `CPF_CNPJ`, `RG`, `ISSUING_ORGANIZATION`, `BIRTH_DATE`, 
          `TELEPHONE`, `CLIENT_TYPE`, `MARITAL_STATUS`, `ADDRESS`, `NATIONALITY`, 
          `PROFESSION`, `NEIGHBORHOOD`, `CITY`, `STATE`, `ZIP`) 
        VALUES
          ('John Doe', '34026867757', '1234567', 'SSP', '1985-05-15', '99999999999', 
            'TENANT', 'SINGLE', '123 Main St', 'Brazilian', 'Engineer', 'Downtown', 'São Paulo', 'SP', '12345678'),
          ('Jane Smith', '15147004416', '2345678', 'SSP', '1990-08-25', '8888888888', 
            'LANDLORD', 'MARRIED', '456 Elm St', 'Brazilian', 'Doctor', 'Midtown', 'Rio de Janeiro', 'RJ', '23456789'),
          ('Alice Johnson', '23147294048', '3456789', 'SSP', '1980-12-05', '7777777777', 
            'TENANT', 'DIVORCED', '789 Oak St', 'Brazilian', 'Lawyer', 'Suburb', 'Belo Horizonte', 'MG', '34567890'),
          ('Bob Williams', '41125054182', '4567890', 'SSP', '1995-03-10', '66666666666', 
            'LANDLORD', 'WIDOWED', '101 Pine St', 'Brazilian', 'Teacher', 'Uptown', 'Curitiba', 'PR', '45678901'),
          ('Charlie Brown', '41682391523', '5678901', 'SSP', '1987-09-30', '55555555555', 
            'TENANT', 'SINGLE', '202 Maple St', 'Brazilian', 'Designer', 'City Center', 'Porto Alegre', 'RS', '56789012'),
          ('Emily Davis', '27755918000105', '6789012', 'SSP', '1992-01-20', '4444444444', 
            'LANDLORD', 'MARRIED', '303 Birch St', 'Brazilian', 'Architect', 'Downtown', 'Florianópolis', 'SC', '67890123'),
          ('Frank Harris', '78451898000101', '7890123', 'SSP', '1975-07-05', '3333333333', 
            'TENANT', 'DIVORCED', '404 Cedar St', 'Brazilian', 'Mechanic', 'Midtown', 'Salvador', 'BA', '78901234'),
          ('Grace Martin', '01249635000190', '8901234', 'SSP', '1983-11-18', '2222222222', 
            'LANDLORD', 'WIDOWED', '505 Walnut St', 'Brazilian', 'Nurse', 'Suburb', 'Fortaleza', 'CE', '89012345'),
          ('Henry Moore', '01249635000190', '9012345', 'SSP', '1989-06-22', '1111111111', 
            'TENANT', 'MARRIED', '606 Aspen St', 'Brazilian', 'Chef', 'Uptown', 'Manaus', 'AM', '90123456'),
          ('Ivy Taylor', '32066470000144', '1234509', 'SSP', '1978-04-15', '0000000000', 
            'LANDLORD', 'SINGLE', '707 Spruce St', 'Brazilian', 'Pilot', 'City Center', 'Recife', 'PE', '01234567'),
          ('Lucas Silva', '12147583900', '2345678', 'SSP', '1988-07-12', '9198887766', 
            'TENANT', 'SINGLE', '808 Cedar St', 'Brazilian', 'Developer', 'Downtown', 'São Paulo', 'SP', '34567890'),
          ('Maria Oliveira', '34158250904', '7654321', 'SSP', '1974-09-18', '9199998888', 
            'LANDLORD', 'MARRIED', '909 Pine St', 'Brazilian', 'Businesswoman', 'Midtown', 'Rio de Janeiro', 'RJ', '45678912'),
          ('Roberto Souza', '43159200800', '3456782', 'SSP', '1992-02-22', '9298887777', 
            'TENANT', 'DIVORCED', '101 Maple St', 'Brazilian', 'Artist', 'Suburb', 'Belo Horizonte', 'MG', '56789013'),
          ('Carla Mendes', '01234613000199', '5432109', 'SSP', '1980-03-11', '9399996666', 
            'LANDLORD', 'WIDOWED', '202 Oak St', 'Brazilian', 'Psychologist', 'Uptown', 'Curitiba', 'PR', '67890134'),
          ('Marcos Pereira', '61247382000187', '6543210', 'SSP', '1985-11-04', '9499995555', 
            'TENANT', 'SINGLE', '303 Birch St', 'Brazilian', 'Photographer', 'City Center', 'Porto Alegre', 'RS', '78901245'),
          ('Tatiane Costa', '17194546200', '7654323', 'SSP', '1990-06-10', '9599994444', 
            'LANDLORD', 'MARRIED', '404 Maple St', 'Brazilian', 'Journalist', 'Downtown', 'Florianópolis', 'SC', '89012356'),
          ('Eduardo Lima', '38125734000193', '8765432', 'SSP', '1972-12-14', '9699993333', 
            'TENANT', 'MARRIED', '505 Oak St', 'Brazilian', 'Musician', 'Midtown', 'Salvador', 'BA', '90123467'),
          ('Beatriz Almeida', '24103642000182', '9876543', 'SSP', '1988-10-25', '9799992222', 
            'LANDLORD', 'SINGLE', '606 Walnut St', 'Brazilian', 'Nurse', 'Suburb', 'Fortaleza', 'CE', '01234578'),
          ('Felipe Rocha', '48715982000168', '1098765', 'SSP', '1994-05-02', '9899991111', 
            'TENANT', 'DIVORCED', '707 Spruce St', 'Brazilian', 'Technician', 'Uptown', 'Manaus', 'AM', '12345679'),
          ('Marcelo Costa', '56487989000123', '6543212', 'SSP', '1993-08-17', '9799991234', 
            'LANDLORD', 'MARRIED', '808 Cedar St', 'Brazilian', 'Pharmacist', 'Downtown', 'São Paulo', 'SP', '23456701'),
          ('Renata Lima', '29182430987', '8765434', 'SSP', '1982-02-28', '9699995678', 
            'TENANT', 'SINGLE', '909 Birch St', 'Brazilian', 'Teacher', 'Midtown', 'Rio de Janeiro', 'RJ', '34567823'),
          ('Augusto Souza', '64414485053', '6549678', 'SSP', '1990-05-28', '12345689735', 
            'LANDLORD', 'SINGLE', '909 Grove St', 'Brazilian', 'Journalist', 'Midtown', 'Rio de Janeiro', 'RJ', '6571236');
      """;
      stmt.executeUpdate(seedClientsSql);

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

      String insertEstatesSql = """
        INSERT INTO `ESTATES` (`ADDRESS`, `NUMBER`, `NEIGHBORHOOD`, `CITY`, `STATE`, `TENANT_ID`, `LANDLORD_ID`, `DESCRIPTION`) 
        VALUES
          ('123 Main St', 101, 'Downtown', 'São Paulo', 'SP', NULL, 2, 'Apartment in the city center, near public transport.'),
          ('456 Elm St', 202, 'Midtown', 'Rio de Janeiro', 'RJ', NULL, 4, 'Spacious house with a large garden.'),
          ('789 Oak St', 303, 'Suburb', 'Belo Horizonte', 'MG', NULL, 6, 'Modern apartment in a quiet neighborhood.'),
          ('101 Pine St', 404, 'Uptown', 'Curitiba', 'PR', NULL, 8, 'Newly built townhouse with a garage.'),
          ('202 Maple St', 505, 'City Center', 'Porto Alegre', 'RS', NULL, 10, 'Condo close to shopping malls and restaurants.'),
          ('303 Birch St', 606, 'Downtown', 'Florianópolis', 'SC', NULL, 12, 'Bright apartment with a sea view.'),
          ('404 Cedar St', 707, 'Midtown', 'Salvador', 'BA', NULL, 14, 'Spacious house with a swimming pool.'),
          ('505 Walnut St', 808, 'Suburb', 'Fortaleza', 'CE', NULL, 16, 'Cozy cottage near parks and green areas.'),
          ('606 Aspen St', 909, 'Uptown', 'Manaus', 'AM', NULL, 18, 'Penthouse apartment in a luxury building.'),
          ('707 Spruce St', 1010, 'City Center', 'Recife', 'PE', NULL, 20, 'Large apartment close to downtown amenities.'),
          ('808 Cedar St', 1111, 'Downtown', 'São Paulo', 'SP', NULL, 22, 'Modern apartment with all necessary facilities.');
      """;
      stmt.executeUpdate(insertEstatesSql);

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

      String createClientContractTableSql = """
        CREATE TABLE IF NOT EXISTS `CLIENT_CONTRACT` (
        CLIENT_ID INT NOT NULL, 
        CONTRACT_ID INT NOT NULL, 
        PRIMARY KEY (CLIENT_ID, CONTRACT_ID),
        FOREIGN KEY (CLIENT_ID) REFERENCES CLIENTS(ID) ON DELETE CASCADE, 
        FOREIGN KEY (CONTRACT_ID) REFERENCES CONTRACTS(ID) ON DELETE CASCADE
        )
      """;
      stmt.executeUpdate(createClientContractTableSql);
    } catch (SQLException e) {
      System.err.println("Error while creating tables: " + e.getMessage());
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
    try (FileInputStream fs = new FileInputStream("db.properties")) {
      Properties props = new Properties();
      props.load(fs);
      return props;
    }
    catch (IOException e) {
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
