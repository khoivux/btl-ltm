package server.dao;

import model.DetailMatch;
import model.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MatchDAO extends DAO{

    public MatchDAO() {
        super();
    }
    /**
     * Lưu một trận đấu mới vào database
     */
    public boolean saveMatch(Match match) {
        String sql = "INSERT INTO tblmatch (start_time, end_time) " +
                "VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        try (Statement stmt = con.createStatement();
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
//    public List<Match> getMatchesByUser(String username) {
//        List<Match> matches = new ArrayList<>();
//
//        String sql = """
//        SELECT DISTINCT m.*
//        FROM tblmatch m
//        JOIN tbldetail_match d ON m.match_id = d.match_id
//        JOIN users u ON d.player_id = u.id
//        WHERE u.username = ?
//        ORDER BY m.start_time DESC
//        """;
//
//        try (PreparedStatement stmt = con.prepareStatement(sql)) {
//            stmt.setString(1, username);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    matches.add(extractMatchFromResultSet(rs));
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Lỗi khi lấy lịch sử trận của " + username + ": " + e.getMessage());
//        }
//
//        return matches;
//    }


    public List<Match> getMatchesByUser(String username) {
        List<Match> matches = new ArrayList<>();

        String sql = """
        SELECT DISTINCT m.*
        FROM tblmatch m
        JOIN tbldetail_match d ON m.match_id = d.match_id
        JOIN users u ON d.player_id = u.id
        WHERE u.username = ?
        ORDER BY m.start_time DESC
        """;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Match match = extractMatchFromResultSet(rs);

                    // ✅ Gọi DAO để lấy danh sách DetailMatch tương ứng
                    DetailMatchDAO detailMatchDAO = new DetailMatchDAO();
                    List<DetailMatch> details = List.of(detailMatchDAO.getDetailsByMatchId(match.getMatchId()));

                    // ✅ Chuyển List -> mảng trước khi set vào Match
                    if (details != null && !details.isEmpty()) {
                        match.setDetailMatch(details.toArray(new DetailMatch[0]));
                    }

                    matches.add(match);
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
        match.setEndTime(rs.getTimestamp("end_time") != null
                ? rs.getTimestamp("end_time").toLocalDateTime()
                : null);
        return match;
    }
}