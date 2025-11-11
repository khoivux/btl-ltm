package client.controller;

import client.Client;
import constant.MessageType;
import constant.Status;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Chat;
import model.Message;
import model.User;

import java.io.IOException;
import java.util.List;

public class MainController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, Status> colStatus;
    @FXML private ListView<Chat> listChats;
    @FXML private TextField addedChat;

    private MediaPlayer mediaPlayer;
    private Client client;
    private ObservableList<User> onlineUsers = FXCollections.observableArrayList();

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void handleLogout() throws IOException {
        stopMusic();
        User logoutUser = client.getUser();
        if(logoutUser != null) {
            Message logoutMessage = new Message(MessageType.LOGOUT, null);
            client.sendMessage(logoutMessage);
        }
    }

    public void updateOnlineUsers(List<User> users) {
        Platform.runLater(() -> {
            onlineUsers.setAll(users);
            userTable.setItems(onlineUsers);
            userTable.refresh();
        });
    }

    public void updateStatusUser(User updatedUser) {
        Platform.runLater(() -> {
            for (int i = 0; i < onlineUsers.size(); i++) {
                User u = onlineUsers.get(i);
                if (u.getUsername().equals(updatedUser.getUsername())) {
                    if (updatedUser.getStatus() == Status.OFFLINE) {
                        onlineUsers.remove(i);
                    } else {
                        onlineUsers.set(i, updatedUser);
                    }
                    userTable.refresh();
                    return;
                }
            }
            if (updatedUser.getStatus() != Status.OFFLINE) {
                onlineUsers.add(updatedUser);
                userTable.refresh();
            }
        });
    }


    public void handleLeaderboard() throws IOException {
        User currentUser = client.getUser();
        if (currentUser != null) {
            try {
                client.sendMessage(new Message(MessageType.RANK, currentUser.getUsername()));
                client.sendMessage(new Message(MessageType.LEADERBOARD, null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        client.showLeaderboardUI();
    }
    //handle match history
    public void handleMatchHistory() throws IOException {
        User currentUser = client.getUser();
        if (currentUser != null) {
            try {
                // Gửi yêu cầu lấy thông tin rank và elo
                client.sendMessage(new Message(MessageType.RANK, currentUser.getUsername()));

                // Gửi yêu cầu lấy lịch sử trận đấu của user
                client.sendMessage(new Message(MessageType.MATCH_HISTORY, currentUser.getUsername()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Hiển thị giao diện lịch sử đấu
        client.showMatchHistoryUI();
    }


    public void handleAddChat(){
        try{
            User user = client.getUser();
            String content = addedChat.getText().trim();
            if (content.isEmpty()){
                System.out.println("Vui long nhap noi dung chat");
            }
            else{
                Message newChat = new Message(MessageType.ADD_CHAT, new Chat(content, user));
                client.sendMessage(newChat);
                addedChat.clear();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateChat(List<Chat> chats){
        Platform.runLater(()->{
            listChats.getItems().clear();
            listChats.getItems().addAll(chats);
            if (!chats.isEmpty()) {
                listChats.scrollTo(chats.size() );
            }
        });
    }

    @FXML
    public void initialize() {
        // Phát nhạc nền
        playBackgroundMusic();

        // Gán dữ liệu cho các cột
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<User, Status>() {
            @Override
            protected void updateItem(Status status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(status.toString());

                // Đổi màu theo trạng thái
                switch (status) {
                    case AVAILABLE:
                        setStyle("-fx-text-fill: #58D58D; -fx-font-weight: bold;");
                        break;
                    case NOT_AVAILABLE:
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        break;
                    case OFFLINE:
                        setStyle("-fx-text-fill: gray;");
                        break;
                    default:
                        setStyle("");
                }
            }
        });

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

        listChats.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // click đúp
                Chat selectedChat = listChats.getSelectionModel().getSelectedItem();
                if (selectedChat != null && selectedChat.getUser() != null && client != null) {
                    client.sendInvite(selectedChat.getUser().getUsername());
                }
            }
        });
    }

    private void playBackgroundMusic() {
        try {
            URL resource = getClass().getResource("/sound/bg-music.mp3");
            if (resource == null) {
                System.out.println("Không tìm thấy tệp nhạc nền.");
                return;
            }
            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
