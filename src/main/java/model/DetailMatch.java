package model;

public class DetailMatch {
    private int id;
    private int point;
    private boolean isWinner;
    private boolean isQuit;

    public DetailMatch(int id, int point, boolean isWinner, boolean isQuit) {
        this.id = id;
        this.point = point;
        this.isWinner = isWinner;
        this.isQuit = isQuit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
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
}
