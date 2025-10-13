package client.controller;

import client.Client;
import constant.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Chat;
import model.Message;
import model.User;

import java.io.IOException;
import java.util.List;

public class MainController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private ListView<Chat> listChats;
    @FXML private TextField addedChat;
//    @FXML private TableColumn<User, String> colStatus;

    private Client client;
    private ObservableList<User> onlineUsers = FXCollections.observableArrayList();

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void handleLogout() throws IOException {
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

    public void handleAddChat(){
        User user = client.getUser();
        String content = addedChat.getText().trim();
    }

    public void updateChat(List<Chat> chats){
        Platform.runLater(()->{
            listChats.getItems().clear();
            listChats.getItems().addAll(chats);
        });
    }

    @FXML
    public void initialize() {
        System.out.println("colUsername = " + colUsername);
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    }
}
