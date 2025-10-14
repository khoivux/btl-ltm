package server.dao;

import model.DetailMatch;
import model.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailMatchDAO extends DAO {
    private Connection connection;

    public DetailMatchDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean saveDetailMatch(DetailMatch detailMatch, int matchId) {
        String sql = "INSERT INTO tbldetail_match (player_id, match_id, score, is_winner, is_quit) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
}
