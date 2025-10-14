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

    public void insertUser(User loginInfo) throws Exception {
        String SQL_QUERY = "INSERT INTO users (username, password, points) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(SQL_QUERY);
        ps.setString(1, loginInfo.getUsername());
        ps.setString(2, loginInfo.getPassword());
        ps.setInt(3, 0);
        ps.executeUpdate();
    }

    public User getUserById(int id){
        String SQL_QUERY = "SELECT * FROM users WHERE id = ?";

        try{
            PreparedStatement ps = con.prepareStatement(SQL_QUERY);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return new User(
                    rs.getInt("id"),
                    rs.getString("username")
                );
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
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



    public User getRankByUsername(String username){
        List<User> users = getLeaderboard();
        User user = new User();
        int i = 0;
        for(i = 0; i < users.size(); i++){
            if(users.get(i).getUsername().equals(username)){
                user = users.get(i);
                break;
            }
        }
        user.setRank(i + 1);
        if(user != null){
            return user;
        }
        return null;
    }
//     public static void main(String[] args) {
//        System.out.println("=== Testing UserDAO ===");

//        UserDAO userDAO = new UserDAO();
//        User user = userDAO.getRankByUsername("lamngu");
//        System.out.println(user.getUsername() + " " + user.getPoints() + " " + user.getRank());

//    }
}


