package org.frobino.contra.ust;

import java.sql.*;
import java.util.Set;

public class PostgreSQLDatabase {
  private Connection connection;

  // JDBC URL, username, and password of PostgreSQL server
  private static String url = "jdbc:postgresql://172.17.0.2:5432/";
  private static String user = "postgres";
  private static String password = "postgrespw";
  private static String dbName = "intervals"; // Change this to the desired database name

  public PostgreSQLDatabase() {
    try {
      String envUrl = System.getenv("CONTRA_DB_URL");
      if (envUrl != null) {
        url = envUrl;
      }
      String envUser = System.getenv("CONTRA_DB_USER");
      if (envUser != null) {
        user = envUser;
      }
      String envPwd = System.getenv("CONTRA_DB_PWD");
      if (envPwd != null) {
        password = envPwd;
      }
      String envDbName = System.getenv("CONTRA_DB_NAME");
      if (envDbName != null) {
        dbName = envDbName;
      }

      // Register PostgreSQL JDBC driver
      Class.forName("org.postgresql.Driver");

      // Open a connection
      connection = DriverManager.getConnection(url, user, password);

      // Create the database if it doesn't exist
      createDatabase();

      // Change the connection URL to point to the "intervals" database
      // connection.setCatalog(dbName);
      try {
        connection = DriverManager.getConnection(url + dbName, user, password);
      } catch (SQLException e) {
        System.out.println("Failed to connect to the DB");
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Connection getConnection() {
    return connection;
  }

  // Method to close the connection
  public void closeConnection() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        System.out.println("Connection closed.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to create a table
  public void createTable(String tableName, String columns) {
    try {
      Statement statement = connection.createStatement();
      String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns + ")";
      statement.executeUpdate(query);
      System.out.println("Table '" + tableName + "' created successfully.");
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Method to add a column to a table if it doesn't exist.
   *
   * @return true if the column was added in the db.
   */
  public boolean addColumnIfNotExists(String tableName, String columnName, String columnType) {
    boolean columnAdded = false;
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName);
      if (!resultSet.next()) {
        Statement statement = connection.createStatement();
        String sql =
            "ALTER TABLE " + tableName + " ADD COLUMN \"" + columnName + "\" " + columnType;
        statement.executeUpdate(sql);
        System.out.println(
            "Column '" + columnName + "' added to table '" + tableName + "' successfully.");
        statement.close();
        columnAdded = true;
      }
      resultSet.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return columnAdded;
  }

  public boolean columnExists(String tableName, String columnName) {
    boolean ret = false;
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName);
      if (resultSet.next()) {
        ret = true;
      }
      resultSet.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return ret;
  }

  private void createDatabase() throws SQLException {
    // Check if the database exists
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet resultSet = metaData.getCatalogs();
    boolean exists = false;
    while (resultSet.next()) {
      String existingDbName = resultSet.getString(1);
      if (existingDbName.equalsIgnoreCase(dbName)) {
        exists = true;
        break;
      }
    }
    resultSet.close();

    // Create the database if it doesn't exist
    if (!exists) {
      Statement statement = connection.createStatement();
      statement.executeUpdate("CREATE DATABASE " + dbName);
      System.out.println("Database '" + dbName + "' created successfully.");
      statement.close();
    }
  }

  public static String generateInsertSpecificValueSql(
      String tableName, String columnName, Object value, long startTime, long endTime) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
    StringBuilder valuesBuilder = new StringBuilder(") VALUES (");

    sqlBuilder.append("\"" + "duration" + "\"" + ", " + "\"" + columnName + "\"");
    // valuesBuilder.append("int8range(1,2), " + value);
    valuesBuilder.append("int8range(" + startTime + "," + ++endTime + "), " + value);

    return sqlBuilder.append(valuesBuilder).append(")").toString();
  }

  public static String generateInsertSql(String tableName, Set<String> columns) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
    StringBuilder valuesBuilder = new StringBuilder(") VALUES (");

    int columnCount = columns.size();
    int counter = 0;
    for (String column : columns) {
      sqlBuilder.append(column);
      valuesBuilder.append("?");
      if (++counter < columnCount) {
        sqlBuilder.append(", ");
        valuesBuilder.append(", ");
      }
    }

    return sqlBuilder.append(valuesBuilder).append(")").toString();
  }

  public void executeUpdate(String sql) {
    Statement statement;
    try {
      statement = connection.createStatement();
      statement.executeUpdate(sql);
      statement.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
