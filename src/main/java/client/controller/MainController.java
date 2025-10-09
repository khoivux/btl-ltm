package client.controller;

import client.Client;
import constant.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Message;
import model.User;

import java.io.IOException;
import java.util.List;

public class MainController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
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

    @FXML
    public void initialize() {
        System.out.println("colUsername = " + colUsername);
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        // double-click a row to send invite
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
}
