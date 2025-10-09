// model/GameSession.java
package model;

import java.time.LocalDateTime;
import java.util.*;

public class GameSession {
    private User player1;
    private User player2;
    private GameBoard board;
    private Map<User, Integer> scores;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public GameSession(User p1, User p2, List<String> targetColors) {
        this.player1 = p1;
        this.player2 = p2;
        this.board = new GameBoard(targetColors);
        this.scores = new HashMap<>();
        scores.put(p1, 0);
        scores.put(p2, 0);
        this.startTime = LocalDateTime.now();
    }

    // Xử lý chọn ô
    public synchronized void pickCell(User player, int row, int col) {
        String cell = board.getCell(row, col);
        if (cell.equals("P1") || cell.equals("P2")) {
            return; // ô đã chọn trước đó
        }

        if (isTargetColor(cell)) {
            scores.put(player, scores.get(player) + 1);
            board.setCell(row, col, player == player1 ? "P1" : "P2");
        } else {
            scores.put(player, scores.get(player) - 1);
        }
    }

    private boolean isTargetColor(String color) {
        // 5 màu ban đầu nằm trong board (cần truyền từ server khi tạo board)
        List<String> targetColors = Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "ORANGE");
        return targetColors.contains(color);
    }

    // Kết thúc ván
    public void endMatch() {
        this.endTime = LocalDateTime.now();
        int score1 = scores.get(player1);
        int score2 = scores.get(player2);

        if (score1 > score2) {
            player1.setPoints(player1.getPoints() + 2);
            player2.setPoints(player2.getPoints());
        } else if (score2 > score1) {
            player2.setPoints(player2.getPoints() + 2);
            player1.setPoints(player1.getPoints());
        } else {
            player1.setPoints(player1.getPoints() + 1);
            player2.setPoints(player2.getPoints() + 1);
        }
    }

    public Map<User, Integer> getScores() {
        return scores;
    }
}
