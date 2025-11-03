package server.dao;

import model.DetailMatch;
import model.*;
import java.util.*;
import java.sql.*;


public class DetailMatchDAO extends DAO {
    public DetailMatchDAO() {
        super();
    }

    public boolean saveDetailMatch(DetailMatch detailMatch, int matchId) {
        String sql = "INSERT INTO tbldetail_match (player_id, match_id, score, is_winner, is_quit) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, detailMatch.getPlayer().getId());
            stmt.setInt(2, matchId);
            stmt.setInt(3, detailMatch.getScore());
            stmt.setBoolean(4, detailMatch.isWinner());
            stmt.setBoolean(5, detailMatch.isQuit());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu thông tin trận đấu: " + e.getMessage());
            return false;
        }
    }
    public DetailMatch[] getDetailsByMatchId(int matchId) {
        List<DetailMatch> details = new ArrayList<>();
        String sql =
                "SELECT dm.id AS detail_id, " +
                        "       dm.score AS detail_score, " +   // alias riêng cho score của DetailMatch
                        "       dm.is_winner, dm.is_quit, " +
                        "       u.id AS user_id, " +
                        "       u.username " +
                        "FROM tbldetail_match AS dm " +
                        "JOIN users AS u ON dm.player_id = u.id " +
                        "WHERE dm.match_id = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User player = new User();
                    player.setId(rs.getInt("user_id"));
                    player.setUsername(rs.getString("username"));
                    player.setPoints(rs.getInt("detail_score")); // lấy score từ alias

                    DetailMatch detail = new DetailMatch();
                    detail.setId(rs.getInt("detail_id"));
                    detail.setPlayer(player);
                    detail.setScore(rs.getInt("detail_score"));
                    detail.setWinner(rs.getBoolean("is_winner"));
                    detail.setQuit(rs.getBoolean("is_quit"));

                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết trận đấu: " + e.getMessage());
        }

        return details.toArray(new DetailMatch[0]);
    }
}

