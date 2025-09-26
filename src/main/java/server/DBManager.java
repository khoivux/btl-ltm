package server;

import model.User;

import java.sql.*;

public class DBManager {
    public static Connection con;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/btl_ltm?autoReconnect=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "khoi21102004";
    private static final String DB_CLASS = "com.mysql.cj.jdbc.Driver";

    public DBManager(){
        if(con == null){
            try {
                Class.forName(DB_CLASS);
                con = DriverManager.getConnection (DB_URL, USER, PASSWORD);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*
    Đăng kí người dùng
     */
    public boolean saveUser(User user) {
        String SQL_QUERY = "INSERT INTO users (username, password, points) VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, 0);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username đã tồn tại: " + user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    Đăng nhập
     */
    public User authenticateUser(User user) {
        String SQL_QUERY = "SELECT * FROM users WHERE username = ? AND password = ?";

        try {
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("points")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
