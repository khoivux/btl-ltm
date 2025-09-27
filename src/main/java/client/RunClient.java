package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.Message;

import java.io.IOException;

public class RunClient extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Client client = new Client(stage);
            client.showLoginUI();
            new Thread(() -> {
                try {
                    client.startConnection("127.0.0.1", 23456);
                } catch (Exception e) {
                    e.printStackTrace();
                    // UI phải cập nhật trên JavaFX Thread
                    Platform.runLater(() -> client.showErrorAlert("Không thể kết nối tới server."));
                }
            }).start();

            // Xử lý khi người dùng đóng cửa sổ
            stage.setOnCloseRequest(event -> {
                try {
                    if (client.getUser() != null) {
                        Message logoutMessage = new Message("logout", client.getUser().getId());
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
