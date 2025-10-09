package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Lớp Match biểu diễn một trận đấu giữa 2 người chơi.
 * Dùng để lưu lịch sử, thống kê kết quả sau trận.
 */
public class Match implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private String player1;          // Tên người chơi 1
    private String player2;          // Tên người chơi 2
    private String winner;           // Ai thắng
    private int player1Score;        // Điểm của người chơi 1
    private int player2Score;        // Điểm của người chơi 2
    private LocalDateTime startTime; // Thời gian bắt đầu trận đấu
    private LocalDateTime endTime;   // Thời gian kết thúc trận đấu


    public Match() {}

    public Match(String player1, String player2, int player1Score, int player2Score, String winner) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.winner = winner;
        this.startTime = LocalDateTime.now();
        this.endTime = LocalDateTime.now();
    }


    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // ===== TO STRING =====
    @Override
    public String toString() {
        return "Match{" +
                "matchId=" + matchId +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                ", winner='" + winner + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}