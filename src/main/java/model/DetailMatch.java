package model;

import java.io.Serializable;

public class DetailMatch implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private User player;
    private int score;
    private boolean isWinner;
    private boolean isQuit;

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }
    public DetailMatch(){}
    public DetailMatch(User player, int point, boolean isWinner, boolean isQuit) {
        this.player = player;
        this.score = point;
        this.isWinner = isWinner;
        this.isQuit = isQuit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setPoint(int point) {
        this.score = point;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    public boolean isQuit() {
        return isQuit;
    }

    public void setQuit(boolean quit) {
        isQuit = quit;
    }

    public void setScore(int score) {this.score = score;}
    }


