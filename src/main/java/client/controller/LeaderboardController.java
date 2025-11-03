package client.controller;

import client.Client;
import constant.MessageType;
import constant.Status;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Message;
import model.User;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class LeaderboardController {
    @FXML
    private Button btnBack;
    //    @FXML
//    private Label userScore;
//    @FXML
//    private Label userRank;
//    @FXML
//    private Label userTop1;
//    @FXML
//    private Label userTop2;
//    @FXML
//    private Label userTop3;
//    @FXML
//    private Label userTop1Score;
//    @FXML
//    private Label userTop2Score;
//    @FXML
//    private Label userTop3Score;
//    @FXML
//    private ListView<User> underTop3Users;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colRank;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, Integer> colPoint;

    private Client client;
    private List<User> users;
    private User user;

    @FXML
    private void initialize() {
        // Auto gọi khi FXML được load
//        if(client != null){
//            loadCurrentUserInfo();
//        }
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPoint.setCellValueFactory(new PropertyValueFactory<>("points"));

        // Double-click vào một hàng để gửi lời mời
        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    User selected = row.getItem();
                    if (selected != null && client != null) {
                        client.sendInvite(selected.getUsername());
                    }
                }
            });
            return row;
        });
    }

//    private void loadCurrentUserInfo(){
//        User curUser = client.getUser();
//        if (curUser != null){
//            userScore.setText("Your score is: " + String.valueOf(curUser.getPoints()));
//            requestUserRank(curUser.getUsername());
//            requestLeaderboard();
//        }
//    }

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
//        loadCurrentUserInfo();
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void handleBack() throws IOException {
        client.showMainUI();
    }


//    @FXML
//    public void updateUserRank(User user) {
//        Platform.runLater(() -> {
//            userScore.setText("Your score is: " + String.valueOf(user.getPoints()));
//            userRank.setText("Rank: " + String.valueOf(user.getRank()));
//        });
//    }



//    public void updateLeaderboard(List<User> topUsers) {
//        if (topUsers == null || topUsers.isEmpty()) {
//            return;
//        }
//        this.users = topUsers;
//        Platform.runLater(() -> {
//            if (users.size() >= 1) {
//                userTop1.setText(users.get(0).getUsername());
//                userTop1Score.setText(String.valueOf(users.get(0).getPoints()));
//            }
//            if (users.size() >= 2) {
//                userTop2.setText(users.get(1).getUsername());
//                userTop2Score.setText(String.valueOf(users.get(1).getPoints()));
//            }
//            if (users.size() >= 3) {
//                userTop3.setText(users.get(2).getUsername());
//                userTop3Score.setText(String.valueOf(users.get(2).getPoints()));
//            }
//
//            // Lấy các users từ vị trí thứ 4 trở đi (không thuộc top 3)
//            ArrayList<User> remainingUsers = new ArrayList<User>();
//            for(int i = 3; i < topUsers.size(); i++){
//                remainingUsers.add(topUsers.get(i));
//            }
//            // Cập nhật ListView với các users còn lại
//            underTop3Users.getItems().clear();
//            underTop3Users.getItems().addAll(remainingUsers);
//        });
//    }

    public void updateLeaderboard(List<User> topUsers) {
        Platform.runLater(() -> {
            if (topUsers != null) {
                this.users = topUsers; // Lưu lại danh sách nếu cần

                // Cách 1: Xóa cũ và thêm mới (đơn giản)
                userTable.getItems().clear();
                userTable.getItems().addAll(topUsers);

                // Cách 2: Tạo ObservableList mới (cách này cũng rất tốt)
                // (Cần import javafx.collections.FXCollections)
                // userTable.setItems(FXCollections.observableArrayList(topUsers));
            }
        });
    }
}
