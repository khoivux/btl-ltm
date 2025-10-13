package server.controller;

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

    public void register(User registerInfo) throws Exception {
        userDAO.insertUser(registerInfo);
    }
}
