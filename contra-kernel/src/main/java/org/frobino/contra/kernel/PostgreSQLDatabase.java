package org.frobino.contra.kernel;

import java.sql.*;
import java.util.List;
import java.util.Map;
import org.eclipse.tracecompass.tmf.core.util.Pair;

public class PostgreSQLDatabase {
  private Connection connection;

  // JDBC URL:
  // Use the following url when running contra.jar locally to fill a postgres container
  // private String url = "jdbc:postgresql://172.17.0.2:5432/";
  // Use the following url when running contra.jar locally to fill a docker-compose with postgres +
  // grafana
  // private String url = "jdbc:postgresql://localhost:5488/";
  // Use the following url when running a docker-compose with contra.jar + postgres + grafana
  private String url = "jdbc:postgresql://pg_data_wh:5432/";

  // Username and password of PostgreSQL server
  private String user = "postgres";
  private String password = "postgrespw";
  private String dbName = "intervals"; // Change this to the desired database name

  public PostgreSQLDatabase() {
    // Override default DB config parameters if specified in env var
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
      while (connection == null) {
        try {
          connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
          System.out.println("Failed to connect to PostgreSQL, retrying in a sec... ");
        }
      }

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
        System.out.println("Database '" + dbName + "' already exists successfully.");
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

  public static String generateInsertSql(
      String tableName, List<String> columns, List<Object> values) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
    StringBuilder valuesBuilder = new StringBuilder(") VALUES (");

    int columnCount = columns.size();
    int counter = 0;
    for (String column : columns) {
      sqlBuilder.append("\"" + column + "\"");
      Object valueToInsert = values.get(counter);
      if (valueToInsert instanceof Pair<?, ?>) {
        Pair<Long, Long> range = (Pair<Long, Long>) valueToInsert;
        // we are trying to insert a range
        valuesBuilder.append("int8range(" + range.getFirst() + "," + (range.getSecond() + 1) + ")");
      } else if (valueToInsert instanceof String) {
        valuesBuilder.append("'" + values.get(counter) + "'");
      } else {
        valuesBuilder.append(values.get(counter));
      }

      if (++counter < columnCount) {
        sqlBuilder.append(", ");
        valuesBuilder.append(", ");
      }
    }

    return sqlBuilder.append(valuesBuilder).append(")").toString();
  }

  /**
   * Generate a single INSERT command with all values to be inserted in a table.
   *
   * @param tableName name of the table where the values will be inserted
   * @param columns a list of strings representing the column names
   * @param values a list of list. Each element of the outmost list is a list containing a row of
   *     the table
   * @return the single INSERT command to be executed to insert values in the table
   */
  public static String generateInsertManySql(
      String tableName, List<String> columns, List<List<Object>> values) {
    StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
    StringBuilder valuesBuilder = new StringBuilder(") VALUES (");

    /*
    * FIXME: the code in this method looks quite ugly.
    * Try to use PreparedStatement instead and check if there is any performance differences compared to the handmade batching.
    *
      PreparedStatement pstmt = null;
      // Prepare the batch insert statement
      pstmt = conn.prepareStatement(INSERT_USER_SQL);

      // Example data to be inserted
      String[][] userData = {
        {"Alice", "alice@example.com"},
        {"Bob", "bob@example.com"},
        {"Charlie", "charlie@example.com"},
        // Add more data as needed
      };

      // Set auto-commit to false
      conn.setAutoCommit(false);

      // Batch the insert operations
      for (String[] user : userData) {
        pstmt.setString(1, user[0]);
        pstmt.setString(2, user[1]);
        pstmt.addBatch();
      }

      // Execute the batch
      pstmt.executeBatch();

      // Commit the transaction
      conn.commit();
    */

    // Create the "columns" part of the query
    for (int counter = 0; counter < columns.size(); counter++) {
      sqlBuilder.append("\"" + columns.get(counter) + "\"");
      if (counter < columns.size() - 1) {
        sqlBuilder.append(", ");
      }
    }

    // Create the "values" part of the query
    int nrOfValues = values.size();
    for (int counter = 0; counter < nrOfValues; counter++) {
      Object valueToInsert = values.get(counter);
      if (valueToInsert instanceof List<?>) {
        int nrElements = ((List<Object>) valueToInsert).size();
        int elCounter = 0;
        for (Object valueElement : (List<Object>) valueToInsert) {
          if (valueElement instanceof Pair<?, ?>) {
            Pair<Long, Long> range = (Pair<Long, Long>) valueElement;
            // we are trying to insert a range
            valuesBuilder.append(
                "int8range(" + range.getFirst() + "," + (range.getSecond() + 1) + ")");
          } else if (valueElement instanceof String) {
            valuesBuilder.append("'" + valueElement + "'");
          } else {
            valuesBuilder.append(valueElement);
          }
          elCounter++;
          if (elCounter < nrElements) {
            valuesBuilder.append(", ");
          }
        }
        if (counter < nrOfValues - 1) {
          valuesBuilder.append("), (");
        }
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

  /**
   * @param sql a parametrized SQL query
   * @param userData a list of arrays where each inner array is composed by the parameters to be put
   *     in the SQL query
   */
  public void batch(final String sql, Map<Integer, String> userData) {
    Connection conn = getConnection();
    // Create a statement
    try {
      // Prepare the batch insert statement
      PreparedStatement pstmt = conn.prepareStatement(sql);
      // Set auto-commit to false
      conn.setAutoCommit(false);

      // Batch the insert operations
      for (Map.Entry<Integer, String> entry : userData.entrySet()) {
        pstmt.setInt(1, entry.getKey());
        pstmt.setString(2, entry.getValue());
        pstmt.addBatch();
      }

      // Execute the batch
      pstmt.executeBatch();

      // Commit the transaction
      conn.commit();

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
