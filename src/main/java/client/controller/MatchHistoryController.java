package client.controller;

import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DetailMatch;
import model.Match;
import model.User;
import model.Message;
import constant.MessageType;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller hiển thị lịch sử các trận đấu của người chơi hiện tại.
 * Nhận dữ liệu từ server (List<Match>) và render lên giao diện JavaFX.
 */
public class MatchHistoryController {

    @FXML
    private Label eloLabel; // hiển thị điểm người chơi

    @FXML
    private Button backButton;

    @FXML
    private TableView<Match> matchTable;

    @FXML
    private TableColumn<Match, String> colOpponent;
    @FXML
    private TableColumn<Match, String> colResult;
    @FXML
    private TableColumn<Match, String> colRatio;
    @FXML
    private TableColumn<Match, String> colStartTime;
    @FXML
    private TableColumn<Match, String> colEndTime;

    private Client client;
    private User user;

    @FXML
    private void initialize() {
        backButton.setOnAction(e -> {
            if (client != null) client.showMainUI();
        });
    }

    private void loadUserInfo() {
        User curUser = (user != null) ? user : (client != null ? client.getUser() : null);

        if (curUser != null) {
            eloLabel.setText("ELO: " + curUser.getPoints());
        } else {
            eloLabel.setText("ELO: N/A");
        }
        requestMatchHistory();
    }

    /** Gửi request lên server để lấy danh sách lịch sử trận đấu */
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
        Platform.runLater(() -> {
            if (matches == null || matches.isEmpty()) {
                matchTable.getItems().clear();
                System.out.println("DEBUG: Nhận được danh sách trận đấu rỗng hoặc null.");
                return;
            }

            matchTable.getItems().clear();

            String currentUsername = (user != null)
                    ? user.getUsername()
                    : (client != null && client.getUser() != null ? client.getUser().getUsername() : "");

            System.out.println("DEBUG: Cập nhật UI cho user: " + currentUsername);
            System.out.println("DEBUG: Nhận được " + matches.size() + " trận đấu.");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            var validMatches = javafx.collections.FXCollections.<Match>observableArrayList();

            Map<Integer, String> opponentMap = new HashMap<>();
            Map<Integer, String> resultMap = new HashMap<>();
            Map<Integer, String> startMap = new HashMap<>();
            Map<Integer, String> endMap = new HashMap<>();
            Map<Integer, String> ratioMap = new HashMap<>();

            for (Match match : matches) {
                DetailMatch[] detailsArr = match.getDetailMatch();
                if (detailsArr == null || detailsArr.length < 2) {
                    System.out.println("DEBUG: Bỏ qua trận ID " + match.getMatchId() + " vì không đủ detail.");
                    continue;
                }

                DetailMatch currentUserDetail = null;
                DetailMatch opponentDetail = null;

                for (DetailMatch detail : detailsArr) {
                    User player = detail.getPlayer();
                    if (player == null || player.getUsername() == null) continue;
                    if (player.getUsername().equals(currentUsername))
                        currentUserDetail = detail;
                    else
                        opponentDetail = detail;
                }

                if (currentUserDetail == null || opponentDetail == null) continue;

                int currentScore = currentUserDetail.getScore();
                int opponentScore = opponentDetail.getScore();
                String ratioText = currentScore + " - " + opponentScore;

                String resultText;
                if (currentUserDetail.isQuit()) {
                    resultText = "QUIT";
                } else if (opponentDetail.isQuit()) {
                    resultText = "VICTORY";
                } else if (currentScore > opponentScore) {
                    resultText = "VICTORY";
                } else if (currentScore < opponentScore) {
                    resultText = "DEFEAT";
                } else {
                    resultText = "DRAW";
                }

                String opponentName = opponentDetail.getPlayer().getUsername();
                String startTimeText = match.getStartTime() != null ? match.getStartTime().format(formatter) : "N/A";
                String endTimeText = match.getEndTime() != null ? match.getEndTime().format(formatter) : "N/A";

                opponentMap.put(match.getMatchId(), opponentName);
                resultMap.put(match.getMatchId(), resultText);
                startMap.put(match.getMatchId(), startTimeText);
                endMap.put(match.getMatchId(), endTimeText);
                ratioMap.put(match.getMatchId(), ratioText);

                validMatches.add(match);
            }

            // Gán dữ liệu cho cột
            colOpponent.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            opponentMap.getOrDefault(cellData.getValue().getMatchId(), "N/A")));

            colResult.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            resultMap.getOrDefault(cellData.getValue().getMatchId(), "N/A")));

            colStartTime.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            startMap.getOrDefault(cellData.getValue().getMatchId(), "N/A")));

            colEndTime.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            endMap.getOrDefault(cellData.getValue().getMatchId(), "N/A")));

            colRatio.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            ratioMap.getOrDefault(cellData.getValue().getMatchId(), "N/A")));

            matchTable.setItems(validMatches);

            // Thiết lập chiều cao động
            matchTable.setFixedCellSize(40);
            double headerHeight = 35;
            double rowHeight = matchTable.getFixedCellSize();
            double calculatedHeight = headerHeight + (validMatches.size() * rowHeight) + 2;
            matchTable.setPrefHeight(Math.min(calculatedHeight, 500));

            // 🔹 Gộp cả tô màu + double click vào cùng một RowFactory
            matchTable.setRowFactory(tableView -> {
                TableRow<Match> row = new TableRow<>() {
                    @Override
                    protected void updateItem(Match match, boolean empty) {
                        super.updateItem(match, empty);
                        if (empty || match == null) {
                            setStyle("");
                        } else {
                            String result = resultMap.get(match.getMatchId());
                            switch (result) {
                                case "VICTORY" -> setStyle("-fx-background-color: #d0f8ce;");
                                case "DEFEAT" -> setStyle("-fx-background-color: #ff9999;");
                                case "DRAW" -> setStyle("-fx-background-color: #fff9c4;");
                                case "QUIT" -> setStyle("-fx-background-color: #e0e0e0;");
                                default -> setStyle("");
                            }
                        }
                    }
                };

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Match selected = row.getItem();
                        DetailMatch[] details = selected.getDetailMatch();

                        for (DetailMatch detail : details) {
                            User player = detail.getPlayer();
                            if (player != null && !player.getUsername().equals(currentUsername)) {
                                System.out.println("DEBUG: Invite " + player.getUsername());
                                if (client != null) client.sendInvite(player.getUsername());
                            }
                        }
                    }
                });

                return row;
            });

            matchTable.refresh();
        });
    }

    public void setClient(Client client) {
        this.client = client;
        loadUserInfo();
        requestMatchHistory();
    }

    public Client getClient() {
        return this.client;
    }

    /** Thiết lập user (nếu muốn override client.getUser()). */
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
