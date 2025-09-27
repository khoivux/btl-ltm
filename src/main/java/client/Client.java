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
        System.out.println("=== BẮT ĐẦU startConnection ===");
        System.out.println("Đang cố kết nối tới server: " + address + ":" + port);
        try {
            System.out.println("=== Tạo Socket ===");
            socket = new Socket(address, port);
            System.out.println("Socket created: " + socket);

            System.out.println("=== Tạo ObjectOutputStream ===");
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            System.out.println("=== Tạo ObjectInputStream ===");
            in = new ObjectInputStream(socket.getInputStream());

            isRunning = true;
            System.out.println("Kết nối server hoàn tất!");

            System.out.println("=== Bắt đầu listenForMessages ===");
            listenForMessages();

        } catch (IOException e) {
            System.out.println("=== LỖI trong startConnection ===");
            e.printStackTrace();
            showErrorAlert("Không thể kết nối tới server.");
        }
    }

    private void listenForMessages() {
        System.out.println("=== BẮT ĐẦU tạo listening thread ===");
        new Thread(() -> {
            System.out.println("=== Listening thread STARTED ===");
            isRunning = true;
            try {
                while (isRunning) {
                    System.out.println("=== Đang chờ message từ server... ===");
                    Message message = (Message) in.readObject();
                    System.out.println("=== Nhận được message: " + (message != null ? message.getType() : "null") + " ===");
                    if (message != null) {
                        handleMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();

                if (isRunning) {
                    System.out.println("=== Connection bị ngắt bất ngờ ===");
                    Platform.runLater(() -> {
                        showLoginUI();
                    });
                } else {
                    System.out.println("=== Đã đóng kết nối, dừng luồng lắng nghe ===");
                }
            }
            System.out.println("=== Listening thread KẾT THÚC ===");
        }).start();
        System.out.println("=== Đã start listening thread ===");
    }

    private void handleMessage(Message message) {
        System.out.println("=== CLIENT NHẬN ĐƯỢC: " + message.getType() + " ===");
        
        switch (message.getType()) {
            case "login_success":
                User user = (User) message.getContent();
                this.user = user;
                System.out.println("Đăng nhập thành công: " + user.getUsername());

                break;
                
            case "login_fail":
                String errorMsg = (String) message.getContent();
                Platform.runLater(() -> {
                    System.out.println("Đăng nhập thất bại: " + errorMsg);
                    if (loginController != null) {
                        loginController.showError(errorMsg);
                    }
                });
                break;
                
            default:
                System.out.println("Unknown message type: " + message.getType());
                break;
        }
    }

    public void showLoginUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/LoginUI.fxml"));
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
    // Thêm debug trong sendMessage()
public void sendMessage(Message message) throws IOException {
    System.out.println("Đang gửi message: " + message.getType());
    if (out == null) {
        System.out.println("ERROR: out is null!");
        return;
    }
    if (socket == null || socket.isClosed()) {
        System.out.println("ERROR: Socket is null or closed!");
        return;
    }
    out.writeObject(message);
    out.flush();
    System.out.println("Message đã gửi thành công");
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
        javafx.application.Application.launch(RunClient.class, args);
    }
}
