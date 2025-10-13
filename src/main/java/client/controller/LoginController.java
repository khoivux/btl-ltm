package client.controller;

import client.Client;
import constant.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import model.Message;
import model.User;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        Message loginMessage = new Message(MessageType.LOGIN, new User(username, password));
        client.sendMessage(loginMessage);
        Message showChat = new Message(MessageType.CHAT, null);
        client.sendMessage(showChat);
    }

    public void showError(String error) {
        Platform.runLater(() -> {
            errorLabel.setText(error);
        });
    }
}
