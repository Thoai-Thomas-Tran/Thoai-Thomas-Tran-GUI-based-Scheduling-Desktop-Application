package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Thoai Tran
 * <br>
 *     This is the Database Connection Class.
 *     This class is in charge of connecting to the SQL DB with the MYSQLJDBC Driver.
 * */
public class DBConnection {

    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String serverName = "//wgudb.ucertify.com:3306/";
    private static final String dbName = "WJ07Xgf";

    //JDBC URL
    private static final String jdbcURL = protocol + vendorName + serverName + dbName;

    //Driver and Connection Interface Reference
    private static final String MYSQLJDBCDriver = "com.mysql.cj.jdbc.Driver";
    private static Connection conn = null;

    private static final String username = "U07Xgf"; //Username
    private static final String password = "53689157077"; //Password

    /**
     * @return conn
     * */
    public static Connection startConnection() {

        try {
            Class.forName(MYSQLJDBCDriver);
            conn = DriverManager.getConnection(jdbcURL, username, password);
            System.out.println("Connection successful!");

        } catch (ClassNotFoundException | SQLException e) {
            //System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return conn;
    }

    /** Get Connection method
     * @return conn
     * */
    public static Connection getConnection(){
        return conn;
    }

    /** Close Connection method
     **/
    public static void closeConnection() {

        try {
            conn.close();
            System.out.println("Connection closed!");

        } catch(SQLException ignored) {
        }
    }

}
