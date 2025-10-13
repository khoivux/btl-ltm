package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import client.controller.LoginController;
import client.controller.MainController;
import client.controller.RegisterController;
import constant.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Message;
import model.User;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;
    private Stage stage;

    // Controllers
    private LoginController loginController;
    private MainController mainController;
    private RegisterController registerController;

    private volatile boolean isRunning = true;

    public Client(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setResizable(false);
    }

    /*
    Connect server
     */
    public void startConnection(String address, int port) {
        System.out.println("Đang cố kết nối tới server: " + address + ":" + port);
        try {
            socket = new Socket(address, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            isRunning = true;
            System.out.println("Kết nối server hoàn tất!");
            System.out.println("=== Bắt đầu listenForMessages ===");
            listenForMessages();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể kết nối tới server.");
        }
    }

    private void listenForMessages() {
        System.out.println("=== BẮT ĐẦU tạo listening thread ===");
        new Thread(() -> {
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
                if (ex instanceof EOFException) {
                    System.out.println("=== Server đã đóng kết nối===");
                } else if (ex instanceof SocketException && ex.getMessage().contains("Socket closed")) {
                    System.out.println("=== Socket đã được đóng ===");
                } else {
                    ex.printStackTrace();
                }

                if (isRunning) {
                    System.out.println("=== Connection bị ngắt  ===");
                    Platform.runLater(() -> {
                        showLoginUI();
                    });
                }
            }
        }).start();
    }

    private void handleMessage(Message message) throws IOException{
        System.out.println("=== CLIENT NHẬN ĐƯỢC: " + message.getType() + " ===");
        
        switch (message.getType()) {
            case MessageType.REGISTER_SUCCESS:
                Platform.runLater(this::showLoginUI);
                break;

            case MessageType.REGISTER_FAILURE:
                String errorMsg = (String) message.getContent();
                Platform.runLater(() -> {
                    if (registerController != null) {
                        registerController.showError(errorMsg);
                    }
                });
                break;

            case MessageType.LOGIN_SUCCESS:
                handleLoginSuccess(message);
                break;
                
            case MessageType.LOGIN_FAILURE:
                handleLoginFailure(message);
                break;

            case MessageType.LOGOUT_SUCCESS:
                handleLogout(message);
                break;

            case MessageType.ONLINE_LIST:
                handleOnlineUsers(message);
                break;

            case MessageType.UPDATE_USER_STATUS:
                sendMessage(new Message(MessageType.ONLINE_LIST, null));
                break;

            default:
                System.out.println("Unknown message type: " + message.getType());
                break;
        }
    }

    private void handleOnlineUsers(Message message) {
        List<User> users = (List<User>) message.getContent();
        users.removeIf(userA -> userA.getUsername().equals(user.getUsername()));
        Platform.runLater(() -> {
            if (mainController != null) {
                mainController.updateOnlineUsers(users);
            }
        });
    }


    private void handleLoginSuccess(Message message) {
        User user = (User) message.getContent();
        this.user = user;
        Platform.runLater(this::showMainUI);
    }

    private void handleLoginFailure(Message message) {
        String errorMsg = (String) message.getContent();
        Platform.runLater(() -> {
            if (loginController != null) {
                loginController.showError(errorMsg);
            }
        });
    }

    private void handleLogout(Message message) {
        this.user = null;
        isRunning = false;
        Platform.runLater(() -> {
            try {
                closeConnection();
                showLoginUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void showLoginUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginUI.fxml"));
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

    public void showMainUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainUI.fxml"));
            Parent root = loader.load();

           mainController = loader.getController();
            if (mainController != null) {
                mainController.setClient(this);
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện chính.");
        }
    }

    public void showRegisterUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterUI.fxml"));
            Parent root = loader.load();

           registerController = loader.getController();
            if (registerController != null) {
                registerController.setClient(this);
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
