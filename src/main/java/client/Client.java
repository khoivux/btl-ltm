package client;

import client.controller.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Message;
import model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;
    private Stage stage;

    // Controllers
    private LoginController loginController;

    private volatile boolean isRunning = true;

    public Client(Stage primaryStage) {
        this.stage = primaryStage;
    }

    /*
    Connect server
     */
    public void startConnection(String address, int port) {
        System.out.println("Đang cố kết nối tới server: " + address + ":" + port);
        try {
            socket = new Socket(address, port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            isRunning = true;
            System.out.println("Kết nối server hoàn tất!");
//            listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể kết nối tới server.");
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            isRunning = true;
            try {
                while (isRunning) {
                    Message message = (Message) in.readObject();
                    if (message != null) {
                        handleMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                if (isRunning) {
                    Platform.runLater(() -> {
                        showLoginUI();

                    });
                } else {
                    System.out.println("Đã đóng kết nối, dừng luồng lắng nghe.");
                }
            }
        }).start();
    }

    private void handleMessage(Message message) {

    }

    public void showLoginUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/LoginUI.fxml"));
            Parent root = loader.load();

            loginController = loader.getController();
            if (loginController != null) {
                loginController.setClient(this);
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện đăng nhập.");
        }
    }


    /*
   Gửi message về server
    */
    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    /*
    Hiển thị lỗi
     */
    public void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public User getUser() {
        return user;
    }

    public void closeConnection() throws IOException {
        isRunning = false; // Dừng luồng lắng nghe
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(AppStarter.class, args);
    }
}
