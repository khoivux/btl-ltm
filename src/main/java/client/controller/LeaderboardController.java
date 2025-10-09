package client.controller;

import client.Client;
import constant.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Message;
import model.User;
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
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void handleBack() throws IOException {
        client.showMainUI();
    }

    @FXML
    public void handleUserRank() throws IOException {
        try{
            String username = client.getUser().getUsername();
            Message message = new Message(MessageType.RANK, username);
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            client.showErrorAlert("Lỗi khi gửi message");
        }
    }

    @FXML
    public void updateUserRank(User user) {
        Platform.runLater(() -> {
            userRank.setText("Rank: " + String.valueOf(user.getRank()));
            userScore.setText("Your score is: " + String.valueOf(user.getPoints()));
        });
    }



    // public void updateLeaderboard() {
    //     users = client.getLeaderboard();
    //     Platform.runLater(() -> {
    //         userTop1.setText(users.get(0).getUsername());
    //         userTop1Score.setText(String.valueOf(users.get(0).getPoints()));
    //         userTop2.setText(users.get(1).getUsername());
    //         userTop2Score.setText(String.valueOf(users.get(1).getPoints()));
    //         userTop3.setText(users.get(2).getUsername());
    //         userTop3Score.setText(String.valueOf(users.get(2).getPoints()));
    //         userRank.setText(String.valueOf(users.get(0).getRank()));
    //         userScore.setText(String.valueOf(users.get(0).getPoints()));
    //     });
    // }
}
