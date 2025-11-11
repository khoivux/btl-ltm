package server.controller;

import model.User;
import server.dao.UserDAO;

import java.util.List;

public class UserController {
    private UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    public User login(User loginInfo) {
        return userDAO.authenticateUser(loginInfo);
    }

    public void register(User registerInfo) throws Exception {
        userDAO.insertUser(registerInfo);
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
