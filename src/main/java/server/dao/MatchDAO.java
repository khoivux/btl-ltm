package server.dao;

import model.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MatchDAO {

    private Connection connection;

    // Constructor
    public MatchDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Lưu một trận đấu mới vào database
     */
    public boolean saveMatch(Match match) {
        String sql = "INSERT INTO matches (player1, player2, player1_score, player2_score, winner, start_time, end_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // match object currently only carries start/end times; callers should extend Match to include details
            // We'll accept nullable fields here
            String p1 = null, p2 = null, winner = null;
            Integer s1 = null, s2 = null;
            try {
                // try to reflectively get fields if available (best-effort), otherwise leave nulls
                java.lang.reflect.Field f;
                f = match.getClass().getDeclaredField("player1");
                f.setAccessible(true);
                Object o = f.get(match);
                if (o != null) p1 = o.toString();
            } catch (Exception ignored) {}
            try {
                java.lang.reflect.Field f = match.getClass().getDeclaredField("player2");
                f.setAccessible(true);
                Object o = f.get(match);
                if (o != null) p2 = o.toString();
            } catch (Exception ignored) {}
            try {
                java.lang.reflect.Field f = match.getClass().getDeclaredField("player1Score");
                f.setAccessible(true);
                Object o = f.get(match);
                if (o instanceof Number) s1 = ((Number) o).intValue();
            } catch (Exception ignored) {}
            try {
                java.lang.reflect.Field f = match.getClass().getDeclaredField("player2Score");
                f.setAccessible(true);
                Object o = f.get(match);
                if (o instanceof Number) s2 = ((Number) o).intValue();
            } catch (Exception ignored) {}
            try {
                java.lang.reflect.Field f = match.getClass().getDeclaredField("winner");
                f.setAccessible(true);
                Object o = f.get(match);
                if (o != null) winner = o.toString();
            } catch (Exception ignored) {}

            stmt.setString(1, p1);
            stmt.setString(2, p2);
            if (s1 != null) stmt.setInt(3, s1); else stmt.setNull(3, java.sql.Types.INTEGER);
            if (s2 != null) stmt.setInt(4, s2); else stmt.setNull(4, java.sql.Types.INTEGER);
            if (winner != null) stmt.setString(5, winner); else stmt.setNull(5, java.sql.Types.VARCHAR);
            stmt.setTimestamp(6, match.getStartTime() != null ? Timestamp.valueOf(match.getStartTime()) : null);
            stmt.setTimestamp(7, match.getEndTime() != null ? Timestamp.valueOf(match.getEndTime()) : null);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu trận đấu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy toàn bộ lịch sử trận đấu
     */
    public List<Match> getAllMatches() {
        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM matches ORDER BY start_time DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                matches.add(extractMatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách trận đấu: " + e.getMessage());
        }
        return matches;
    }

    /**
     * Lấy lịch sử trận đấu của người chơi
     */
    public List<Match> getMatchesByUser(String username) {
        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM matches WHERE player1 = ? OR player2 = ? ORDER BY start_time DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(extractMatchFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử trận của " + username + ": " + e.getMessage());
        }
        return matches;
    }

    /**
     * Hàm tiện ích để chuyển ResultSet -> Match object
     */
    private Match extractMatchFromResultSet(ResultSet rs) throws SQLException {
        Match match = new Match();
        match.setMatchId(rs.getInt("match_id"));
        match.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        match.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        return match;
    }
}