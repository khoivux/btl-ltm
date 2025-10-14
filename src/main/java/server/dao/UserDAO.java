package server.dao;

import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DAO{
    public UserDAO() {
        super();
    }

    public boolean saveUser(User loginInfo) {
        String SQL_QUERY = "INSERT INTO users (username, password, score) VALUES (?, ?, ?)";

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

    public boolean savePoint(User user, int point){
        String SQL_QUERY = "UPDATE `btl_ltm`.`users ` SET `points` = ? WHERE (`id` = ?);";
        try {
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setInt(1, point);
            ps.setInt(2, user.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User authenticateUser(User user) {
        String SQL_QUERY = "SELECT * FROM users  WHERE username = ? AND password = ?";

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

    public List<User> getList() {
        List<User> users = new ArrayList<>();
        String SQL_QUERY = "SELECT * FROM users ";

        try {
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("points")
                );
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

}
