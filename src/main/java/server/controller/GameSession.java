// model/GameSession.java
package server.controller;

import model.DetailMatch;
import model.Match;
import model.User;
import server.GameBoardManager;
import server.dao.DetailMatchDAO;
import server.dao.MatchDAO;
import server.dao.UserDAO;

import java.time.LocalDateTime;
import java.util.*;

public class GameSession {
    private User player1;
    private User player2;
    private GameBoardManager board;
    // use username as key to avoid relying on User.equals/hashCode
    private Map<String, Integer> scores;
    private List<String> targetColors;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MatchDAO matchDAO;
    private DetailMatchDAO detailMatchDAO;


    public GameSession(User p1, User p2, List<String> targetColors) {
        this.player1 = p1;
        this.player2 = p2;
        this.targetColors = new ArrayList<>(Objects.requireNonNull(targetColors));
        this.board = new GameBoardManager(this.targetColors);
        this.scores = new HashMap<>();
        scores.put(p1.getUsername(), 0);
        scores.put(p2.getUsername(), 0);
        this.startTime = LocalDateTime.now();
        try {
            // lazily initialize MatchDAO using the shared DAO connection
            this.matchDAO = new MatchDAO();
            this.detailMatchDAO = new DetailMatchDAO();
        } catch (Exception ex) {
            // if DB is not available, just log and continue - match saving will be skipped
            System.err.println("GameSession: Không thể khởi tạo DAO: " + ex.getMessage());
            this.matchDAO = null;
        }
    }

    public GameBoardManager getBoard(){
        return this.board;
    }

    public String getPlayer1Username() { return player1.getUsername(); }
    public String getPlayer2Username() { return player2.getUsername(); }

    // Xử lý chọn ô - trả về thông tin chi tiết để server gửi tới clients
    public static class PickResult {
        public final boolean valid; // false nếu input không hợp lệ (out of bounds)
        public final boolean hit; // true nếu đúng màu target
        public final boolean locked; // true nếu ô đã bị chọn trước đó
        public final String marker; // "P1" hoặc "P2"
        public final int scoreP1;
        public final int scoreP2;
        public final String message;

        public PickResult(boolean valid, boolean hit, boolean locked, String marker,
                          int scoreP1, int scoreP2, String message) {
            this.valid = valid;
            this.hit = hit;
            this.locked = locked;
            this.marker = marker;
            this.scoreP1 = scoreP1;
            this.scoreP2 = scoreP2;
            this.message = message;
        }
    }

    public synchronized PickResult pickCell(User player, int row, int col) {
        // bounds check
        if (row < 0 || col < 0) {
            return new PickResult(false, false, false, null, scores.get(getPlayer1Username()), scores.get(getPlayer2Username()), "Invalid coordinates");
        }
        try {
            String cell = board.getCell(row, col);
            if ("P1".equals(cell) || "P2".equals(cell)) {
                return new PickResult(true, false, true, null, scores.get(getPlayer1Username()), scores.get(getPlayer2Username()), "Cell already taken");
            }

            boolean hit = isTargetColor(cell);
            String username = player.getUsername();
            String marker = username.equals(getPlayer1Username()) ? "P1" : "P2";

            if (hit) {
                int prev = scores.getOrDefault(username, 0);
                scores.put(username, prev + 1);
                board.setCell(row, col, marker);
            } else {
                int prev = scores.getOrDefault(username, 0);
                scores.put(username, Math.max(prev - 1, 0));
            }
            System.out.println("picked: " + cell);
            return new PickResult(true, hit, false, marker, scores.get(getPlayer1Username()), scores.get(getPlayer2Username()), hit ? "Hit" : "Miss");
        } catch (ArrayIndexOutOfBoundsException ex) {
            return new PickResult(false, false, false, null, scores.get(getPlayer1Username()), scores.get(getPlayer2Username()), "Out of bounds");
        }
    }

    private boolean isTargetColor(String color) {
        if (color == null) return false;
        // if cell already marked as P1/P2 -> not a color
        if ("P1".equals(color) || "P2".equals(color)) return false;
        return targetColors.contains(color);
    }

    // Kết thúc ván - trả về kết quả chấm điểm
    public static class MatchResult {
        public final int score1;
        public final int score2;
        public final String winner; // null nếu hòa
        public final int awardP1; // points awarded to player1 (2/1/0)
        public final int awardP2;

        public MatchResult(int score1, int score2, String winner, int awardP1, int awardP2) {
            this.score1 = score1;
            this.score2 = score2;
            this.winner = winner;
            this.awardP1 = awardP1;
            this.awardP2 = awardP2;
        }
    }

    public MatchResult endMatch(String usernameQuit) {
        this.endTime = LocalDateTime.now();
        int score1 = scores.getOrDefault(getPlayer1Username(), 0);
        int score2 = scores.getOrDefault(getPlayer2Username(), 0);

        int award1 = 0, award2 = 0;
        String winner = null;
        
        // Nếu có người quit, người đó thua và không được điểm
        if (usernameQuit != null && !usernameQuit.isEmpty()) {
            if (usernameQuit.equals(getPlayer1Username())) {
                // Player1 quit -> Player2 thắng
                award1 = 0;  // Người quit không nhận điểm
                award2 = 2;  // Người còn lại thắng và nhận 2 điểm
                winner = getPlayer2Username();
                System.out.println("Player1 (" + getPlayer1Username() + ") quit. Player2 wins!");
            } else if (usernameQuit.equals(getPlayer2Username())) {
                // Player2 quit -> Player1 thắng
                award1 = 2;  // Người còn lại thắng và nhận 2 điểm
                award2 = 0;  // Người quit không nhận điểm
                winner = getPlayer1Username();
                System.out.println("Player2 (" + getPlayer2Username() + ") quit. Player1 wins!");
            }
        } else {
            // Trận đấu kết thúc bình thường theo điểm số
            if (score1 > score2) {
                award1 = 2;
                winner = getPlayer1Username();
            } else if (score2 > score1) {
                award2 = 2;
                winner = getPlayer2Username();
            } else {
                // Hòa
                award1 = 1;
                award2 = 1;
            }
        }

        // update users' points
        player1.setPoints(player1.getPoints() + award1);
        player2.setPoints(player2.getPoints() + award2);

        if (matchDAO != null) {
            Match m = new Match(
                    startTime,
                    endTime
            );
            DetailMatch detailMatch1 = new DetailMatch(
                    player1,
                    score1,
                    winner != null && winner.equals(getPlayer1Username()),
                    getPlayer1Username().equals(usernameQuit)
            );
            DetailMatch detailMatch2 = new DetailMatch(
                    player2,
                    score2,
                    winner != null && winner.equals(getPlayer2Username()),
                    getPlayer2Username().equals(usernameQuit)
            );
            m.setDetailMatch(new DetailMatch[]{detailMatch1, detailMatch2});
            boolean savematch = matchDAO.saveMatch(m);
            if (savematch)
            {
                if(detailMatchDAO != null){
                    boolean savedetail = detailMatchDAO.saveDetailMatch(detailMatch1, m.getMatchId());
                    if(!savedetail){System.err.println("Có lỗi xảy ra khi lưu thông tin trận đấu của " + getPlayer1Username());}

                    savedetail = detailMatchDAO.saveDetailMatch(detailMatch2, m.getMatchId());
                    if(!savedetail){System.err.println("Có lỗi xảy ra khi lưu thông tin trận đấu của " + getPlayer2Username());}
                }else {
                    System.out.println("DetailMatchDAO chưa được khởi tạo.");
                }
            } else{
                System.err.println("Có lỗi xảy ra khi lưu thông tin trận đấu giữa " + getPlayer1Username() + " và " + getPlayer2Username());
            }
        } else {
            System.out.println("MatchDAO chưa được khởi tạo.");
        }
        return new MatchResult(score1, score2, winner, award1, award2);
    }

    public Map<String, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }
}
