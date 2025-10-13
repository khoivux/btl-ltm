package client.controller;

import client.Client;
import constant.MessageType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Message;
import model.User;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label errorLabel;
    
    private Client client;
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        } else if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        } else if (!password.equals(confirmPassword)) {
            showError("Mật khẩu nhập lại không khớp!");
            return;
        }

        errorLabel.setText("");

        try {
            User newUser = new User(username, password);
            Message registerMessage = new Message(MessageType.REGISTER, newUser);
            client.sendMessage(registerMessage);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi kết nối tới server!");
        }
    }
    
    public void showError(String message) {
        errorLabel.setText(message);
    }

    
    public void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        errorLabel.setText("");
        errorLabel.setTextFill(javafx.scene.paint.Color.RED);
    }
}