package client.controller;

import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.DetailMatch;
import model.Match;
import model.User;
import model.Message;
import constant.MessageType;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller hiển thị lịch sử các trận đấu của người chơi hiện tại.
 * Nhận dữ liệu từ server (List<Match>) và render lên giao diện JavaFX.
 */
public class MatchHistoryController {

    @FXML
    private VBox matchListBox; // VBox chứa danh sách trận đấu

    @FXML
    private Label eloLabel; // hiển thị điểm ELO

    @FXML
    private Button backButton;

    private Client client;
    private User user;

    @FXML
    private void initialize() {
        backButton.setOnAction(e -> {
            if (client != null) client.showMainUI();
        });
    }

    /**tblmatch
     * Hiển thị ELO từ user hiện tại (nếu null thì hiện N/A)
     */
    private void loadUserInfo() {
        User curUser = (user != null) ? user : (client != null ? client.getUser() : null);

        if (curUser != null) {
            eloLabel.setText("ELO: " + curUser.getPoints());
        } else {
            eloLabel.setText("ELO: N/A");
        }
        requestMatchHistory();
    }

    /**
     * Gửi request lên server để lấy danh sách lịch sử trận đấu của user hiện tại.
     */
    private void requestMatchHistory() {
        User curUser = (user != null) ? user : (client != null ? client.getUser() : null);
        if (curUser == null || client == null) return;

        try {
            Message message = new Message(MessageType.MATCH_HISTORY, curUser.getUsername());
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            if (client != null)
                client.showErrorAlert("Lỗi khi gửi request lấy lịch sử trận đấu");
        }
    }

    /**
     * Cập nhật giao diện hiển thị danh sách các trận đấu.
     * @param matches Danh sách các trận (nhận từ server)
     */
    public void updateMatchHistory(List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            Platform.runLater(() -> {
                matchListBox.getChildren().clear();
                Label emptyLabel = new Label("Chưa có trận đấu nào.");
                emptyLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
                matchListBox.getChildren().add(emptyLabel);

                System.out.println("DEBUG: Nhận được danh sách trận đấu rỗng hoặc null."); // DEBUG
            });
            return;
        }

        Platform.runLater(() -> {
            matchListBox.getChildren().clear();
            String currentUsername = (user != null)
                    ? user.getUsername()
                    : (client != null && client.getUser() != null ? client.getUser().getUsername() : "");

            System.out.println("DEBUG: Cập nhật UI cho user: " + currentUsername); // DEBUG
            System.out.println("DEBUG: Nhận được " + matches.size() + " trận đấu."); // DEBUG

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            int matchesAdded = 0; // Đếm số trận được thêm

            for (Match match : matches) {
                List<DetailMatch> details = List.of(match.getDetailMatch());
                if (details == null || details.size() < 2) {
                    System.out.println("DEBUG: Bỏ qua trận (ID: " + match.getMatchId() + ") vì không đủ detail."); // DEBUG
                    continue;
                }

                DetailMatch currentUserDetail = null;
                DetailMatch opponentDetail = null;

                for (DetailMatch detail : details) {
                    User player = detail.getPlayer();
                    if (player == null || player.getUsername() == null) { // KIỂM TRA KỸ
                        System.out.println("DEBUG: Bỏ qua detail vì player hoặc username bị null."); // DEBUG
                        continue;
                    }

                    System.out.println("DEBUG: Đang kiểm tra player: " + player.getUsername()); // DEBUG

                    if (player.getUsername().equals(currentUsername)) {
                        currentUserDetail = detail;
                    } else {
                        opponentDetail = detail;
                    }
                }

                if (currentUserDetail == null || opponentDetail == null) {
                    System.out.println("DEBUG: Bỏ qua trận (ID: " + match.getMatchId() + ") vì không tìm thấy currentUser hoặc opponent."); // DEBUG
                    if(currentUserDetail == null) System.out.println("DEBUG: -> currentUserDetail bị null");
                    if(opponentDetail == null) System.out.println("DEBUG: -> opponentDetail bị null");
                    continue;
                }

                // ... (code tạo Label của bạn) ...
                String opponentName = opponentDetail.getPlayer().getUsername();

// Lấy điểm của 2 người chơi
                int currentScore = currentUserDetail.getScore();
                int opponentScore = opponentDetail.getScore();

// Xác định kết quả dựa trên so sánh điểm
                String resultText;
                String resultColor;
                if (currentScore > opponentScore) {
                    resultText = "VICTORY";
                    resultColor = "#00ff99";
                } else if (currentScore < opponentScore) {
                    resultText = "DEFEAT";
                    resultColor = "#ff4655";
                } else {
                    resultText = "DRAW";
                    resultColor = "#ffff66"; // màu vàng cho hòa
                }

// --- Tạo các label hiển thị thông tin ---
                Label opponentLabel = new Label("vs " + opponentName);
                opponentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

                Label resultLabel = new Label(resultText);
                resultLabel.setStyle("-fx-text-fill: " + resultColor + "; -fx-font-weight: bold;");

// Label hiển thị tỉ số
                Label scoreLabel = new Label(currentScore + " - " + opponentScore);
                scoreLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");

                String startTime = (match.getStartTime() != null)
                        ? match.getStartTime().format(formatter)
                        : "N/A";
                String endTime = (match.getEndTime() != null)
                        ? match.getEndTime().format(formatter)
                        : "N/A";

                Label timeLabel = new Label("Bắt đầu: " + startTime + " | Kết thúc: " + endTime);
                timeLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

// --- Gộp hàng hiển thị ---
                VBox matchInfo = new VBox(5, opponentLabel, resultLabel, scoreLabel, timeLabel);
                matchInfo.setPadding(new Insets(10));
                matchInfo.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10;");

                matchListBox.getChildren().add(matchInfo);
                matchesAdded++; // Tăng bộ đếm
            }

            System.out.println("DEBUG: Đã thêm " + matchesAdded + " trận vào UI."); // DEBUG
        });
    }

    /**
     * Thiết lập client cho controller.
     * Gọi khi load FXML từ Client.showMatchHistoryUI().
     */
    public void setClient(Client client) {
        this.client = client;
        loadUserInfo();
        requestMatchHistory();
    }

    public Client getClient() {
        return this.client;
    }

    /**
     * Thiết lập user (nếu muốn override client.getUser()).
     */
    public void setUser(User user) {
        this.user = user;
        loadUserInfo();
        requestMatchHistory();
    }

    @FXML
    private void handleBack() throws IOException {
        if (client != null) client.showMainUI();
    }
}
