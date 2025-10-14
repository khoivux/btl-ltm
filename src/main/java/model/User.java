package model;

import server.dao.UserDAO;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String username;
    private String password;
    private String status;
    private int points;
    private int rank;

    public User() {
    }


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(int id, String username, int points) {
        this.id = id;
        this.username = username;
        this.points = points;
    }

    public User(String username, int points, int rank) {
        this.username = username;
        this.points = points;
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoints() {
        return points;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPoints(int points) {
        this.points = points;
        UserDAO userDAO = new UserDAO();
        userDAO.savePoint(this, points);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return String.format("#%d \t %s \t\t %dpts", rank, username, points);
    }
}