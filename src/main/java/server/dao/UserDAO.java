package server.dao;

import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;

public class UserDAO extends DAO{
    public UserDAO() {
        super();
    }

    public boolean saveUser(User loginInfo) {
        String SQL_QUERY = "INSERT INTO users (username, password, points) VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, loginInfo.getUsername());
            ps.setString(2, loginInfo.getPassword());
            ps.setInt(3, 0);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username đã tồn tại: " + loginInfo.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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
