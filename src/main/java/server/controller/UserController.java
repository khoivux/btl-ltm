package server.controller;

import java.util.List;
import model.User;
import server.dao.UserDAO;

public class UserController {
    private UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    public User login(User loginInfo) {
        return userDAO.authenticateUser(loginInfo);
    }

    public List<User> getLeaderboard() {
        return userDAO.getLeaderboard();
    }

    public User getRankByUsername(String username) {
        return userDAO.getRankByUsername(username);
    }

    public User getUserById(int id){
        return userDAO.getUserById(id);
    }
}
