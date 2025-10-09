package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO  {
    public static Connection con;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/btl_ltm?autoReconnect=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    private static final String DB_CLASS = "com.mysql.cj.jdbc.Driver";

    public DAO(){
        if(con == null){
            try {
                Class.forName(DB_CLASS);
                con = DriverManager.getConnection (DB_URL, USER, PASSWORD);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                con = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
