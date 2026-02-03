
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
  private static final String DRIVER_NAME = System.getenv("DRIVER_NAME");
  private static final String DB_URL = System.getenv("DB_URL");
  private static final String USER_DB = System.getenv("USER_DB");
  private static final String PASS_DB = System.getenv("PASS_DB");

  /**
   * Get connection to database
   * 
   * @return connection to database
   */
  public static Connection getConnection() {
    Connection con = null;
    try {
      Class.forName(DRIVER_NAME);
      con = DriverManager.getConnection(DB_URL, USER_DB, PASS_DB);
    } catch (ClassNotFoundException | SQLException e) {
      Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, e);
    }
    return con;
  }

  /**
   * Main method to test connection
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    try (Connection con = getConnection()) {
      if (con != null)
        System.out.println("Connect to DB successfully!");
    } catch (Exception ex) {
      Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
