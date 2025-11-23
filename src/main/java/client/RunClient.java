package client;

import constant.MessageType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.Message;
import model.User;

import java.io.IOException;

public class RunClient extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Client client = new Client(primaryStage);
            client.showLoginUI();
            new Thread(() -> {
                try { // IP của máy chủ trong Radmin VPN là 26.41.147.33
                    client.startConnection("26.41.147.33", 23456);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> client.showErrorAlert("Không thể kết nối tới server."));
                }
            }).start();

            // Xử lý khi người dùng đóng cửa sổ
            primaryStage.setOnCloseRequest(event -> {
                try {
                    User user = client.getUser();
                    if (user != null) {
                        Message logoutMessage = new Message(MessageType.LOGOUT, user);
                        client.sendMessage(logoutMessage);
                    }
                    client.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }
}
