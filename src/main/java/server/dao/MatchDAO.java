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
        String sql = "INSERT INTO tblmatch (start_time, end_time) " +
                "VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, match.getStartTime() != null ? Timestamp.valueOf(match.getStartTime()) : null);
            stmt.setTimestamp(2, match.getEndTime() != null ? Timestamp.valueOf(match.getEndTime()) : null);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // Lấy matchId được sinh tự động
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        match.setMatchId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu trận đấu: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy toàn bộ lịch sử trận đấu
     */
    public List<Match> getAllMatches() {
        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM tblmatch ORDER BY start_time DESC";
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
        String sql = "SELECT * FROM tblmatch WHERE player1 = ? OR player2 = ? ORDER BY start_time DESC";
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
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) match.setStartTime(st.toLocalDateTime());
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) match.setEndTime(et.toLocalDateTime());
        return match;
    }
}