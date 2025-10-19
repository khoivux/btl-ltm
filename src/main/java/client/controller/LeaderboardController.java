package client.controller;

import client.Client;
import constant.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Message;
import model.User;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class LeaderboardController {
    @FXML
    private Button btnBack;
    @FXML
    private Label userScore;
    @FXML
    private Label userRank;
    @FXML
    private Label userTop1;
    @FXML
    private Label userTop2;
    @FXML
    private Label userTop3;
    @FXML
    private Label userTop1Score;
    @FXML
    private Label userTop2Score;
    @FXML
    private Label userTop3Score;
    @FXML
    private ListView<User> underTop3Users;

    private Client client;
    private List<User> users;
    private User user;

    @FXML
    private void initialize() {
        // Auto gọi khi FXML được load
        if(client != null){
            loadCurrentUserInfo();
        }
    }

    private void loadCurrentUserInfo(){
        User curUser = client.getUser();
        if (curUser != null){
            userScore.setText("Your score is: " + String.valueOf(curUser.getPoints()));
            requestUserRank(curUser.getUsername());
            requestLeaderboard();
        }
    }

    private void requestLeaderboard(){
        try{
            Message message = new Message(MessageType.LEADERBOARD, null);
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            client.showErrorAlert("Lỗi khi gửi message leaderboard");
        }
    }

    private void requestUserRank(String username){
        try{
            Message message = new Message(MessageType.RANK, username);
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            client.showErrorAlert("Lỗi khi gửi message");
        }
    }

    public void setClient(Client client) {
        this.client = client;
        // When client is set after FXML load, trigger initial data fetch
        loadCurrentUserInfo();
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void handleBack() throws IOException {
        client.showMainUI();
    }


    @FXML
    public void updateUserRank(User user) {
        Platform.runLater(() -> {
            userScore.setText("Your score is: " + String.valueOf(user.getPoints()));
            userRank.setText("Rank: " + String.valueOf(user.getRank()));
        });
    }



    public void updateLeaderboard(List<User> topUsers) {
        if (topUsers == null || topUsers.isEmpty()) {
            return;
        }
        this.users = topUsers;
        Platform.runLater(() -> {
            if (users.size() >= 1) {
                userTop1.setText(users.get(0).getUsername());
                userTop1Score.setText(String.valueOf(users.get(0).getPoints()));
            }
            if (users.size() >= 2) {
                userTop2.setText(users.get(1).getUsername());
                userTop2Score.setText(String.valueOf(users.get(1).getPoints()));
            }
            if (users.size() >= 3) {
                userTop3.setText(users.get(2).getUsername());
                userTop3Score.setText(String.valueOf(users.get(2).getPoints()));
            }

            // Lấy các users từ vị trí thứ 4 trở đi (không thuộc top 3)
            ArrayList<User> remainingUsers = new ArrayList<User>();
            for(int i = 3; i < topUsers.size(); i++){
                remainingUsers.add(topUsers.get(i));
            }
            // Cập nhật ListView với các users còn lại
            underTop3Users.getItems().clear();
            underTop3Users.getItems().addAll(remainingUsers);
        });
    }
}
