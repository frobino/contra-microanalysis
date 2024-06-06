package org.frobino.contra.kernel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

public class Postgres implements ITmfStateSystemBuilder {

  // JDBC URL, username, and password of PostgreSQL server
  private static final String url = "jdbc:postgresql://172.17.0.2:5432/";
  private static final String user = "postgres";
  private static final String password = "postgrespw";
  private static final String dbName = "intervals"; // Change this to the desired database name

  private Connection connection;

  // Constructor
  public Postgres() {
    try {

      // Register PostgreSQL JDBC driver
      Class.forName("org.postgresql.Driver");

      // Open a connection
      connection = DriverManager.getConnection(url, user, password);

      // Create the database if it doesn't exist
      createDatabase();

      // Change the connection URL to point to the "intervals" database
      connection.setCatalog(dbName);

      /*
      // Execute SQL query
      Statement statement = fConnection.createStatement();
      String query = "SELECT * FROM intervals";
      ResultSet resultSet = statement.executeQuery(query);

      // Process the result set
      while (resultSet.next()) {
          // Retrieve data from each row
          int id = resultSet.getInt("entry_id");
          String name = resultSet.getString("thread_name");
          // Print the data
          System.out.println("ID: " + id + ", Name: " + name);
      }

      // Close resources
      resultSet.close();
      statement.close();
      */
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub

  }

  @Override
  public @NonNull String getAttributeName(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getCurrentEndTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public @NonNull String getFullAttributePath(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String @NonNull [] getFullAttributePathArray(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getNbAttributes() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getOngoingStartTime(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getParentAttributeQuark(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getQuarkAbsolute(String... arg0) throws AttributeNotFoundException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getQuarkRelative(int arg0, String... arg1) throws AttributeNotFoundException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public @NonNull List<@NonNull Integer> getQuarks(String... arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull List<@NonNull Integer> getQuarks(int arg0, String... arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull String getSSID() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getStartTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public @NonNull List<@NonNull Integer> getSubAttributes(int arg0, boolean arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull List<@NonNull Integer> getSubAttributes(int arg0, boolean arg1, String arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isCancelled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int optQuarkAbsolute(String... arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int optQuarkRelative(int arg0, String... arg1) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Iterable<@NonNull ITmfStateInterval> query2D(
      @NonNull Collection<Integer> arg0, @NonNull Collection<Long> arg1)
      throws StateSystemDisposedException, IndexOutOfBoundsException, TimeRangeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<@NonNull ITmfStateInterval> query2D(
      @NonNull Collection<Integer> arg0, long arg1, long arg2)
      throws StateSystemDisposedException, IndexOutOfBoundsException, TimeRangeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull List<@NonNull ITmfStateInterval> queryFullState(long arg0)
      throws StateSystemDisposedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull ITmfStateValue queryOngoingState(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull ITmfStateInterval querySingleState(long arg0, int arg1)
      throws StateSystemDisposedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void waitUntilBuilt() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean waitUntilBuilt(long arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void closeHistory(long arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getQuarkAbsoluteAndAdd(String... arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getQuarkRelativeAndAdd(int arg0, String... arg1) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void modifyAttribute(long arg0, Object arg1, int arg2) throws StateValueTypeException {
    // TODO Auto-generated method stub

  }

  @Override
  public ITmfStateValue popAttribute(long arg0, int arg1) throws StateValueTypeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void pushAttribute(long arg0, Object arg1, int arg2) throws StateValueTypeException {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeAttribute(long arg0, int arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeFiles() {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateOngoingState(@NonNull ITmfStateValue arg0, int arg1) {
    // TODO Auto-generated method stub

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
}
