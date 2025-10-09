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
            stmt.setTimestamp(6, Timestamp.valueOf(match.getStartTime()));
            stmt.setTimestamp(7, Timestamp.valueOf(match.getEndTime()));
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