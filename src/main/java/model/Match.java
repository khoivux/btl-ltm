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
    private LocalDateTime startTime; // Thời gian bắt đầu trận đấu
    private LocalDateTime endTime;   // Thời gian kết thúc trận đấu
    private DetailMatch detailMatch[];


    public Match() {}

    public DetailMatch[] getDetailMatch() {
        return detailMatch;
    }

    public void setDetailMatch(DetailMatch[] detailMatch) {
        this.detailMatch = detailMatch;
    }

    public Match(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
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
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}