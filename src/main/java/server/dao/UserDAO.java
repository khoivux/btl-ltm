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

    public List<User> getList() {
        List<User> users = new ArrayList<>();
        String SQL_QUERY = "SELECT * FROM users";

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

    public List<User> getLeaderboard() {
        List<User> users = new ArrayList<>();
        String SQL_QUERY = "SELECT username, points FROM users ORDER BY points DESC";
        
        try {
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getInt("points"),
                    rank
                );
                users.add(user);
                rank++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getRankByUsername(String username) {
        String SQL_QUERY ="SELECT t.username, t.points, t.rank FROM (" +
            "  SELECT u.username, DENSE_RANK() OVER (ORDER BY u.points DESC) AS rank" +
            "  FROM users u" +
            ") t WHERE t.username = ?";

        try{
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getInt("points"),
                    rs.getInt("rank")
                );
                return user;
            }
        }   
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
