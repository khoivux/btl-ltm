package client;

import client.controller.LoginController;
import client.controller.MainController;
import client.controller.LeaderboardController;
import client.controller.RegisterController;
import constant.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import model.Chat;
import model.Message;
import model.User;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;
    private Stage stage;


    private LoginController loginController;
    private LeaderboardController leaderboardController;
    private MainController mainController;
    private RegisterController registerController;

    private volatile boolean isRunning = true;
    // Game controller reference
    private client.controller.GameController gameController;

    // Khởi tạo client
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
    /*
     * hàm nghe message từ server
     */
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
        // if (message.getType().equals(MessageType.RANK_SUCCESS)){
        //     System.out.println(message.getContent().getClass());
        // }

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

            case MessageType.UPDATE_USER_STATUS:
                mainController.updateStatusUser((User) message.getContent());
                break;

            case MessageType.RANK_SUCCESS:
                handleUserRankSuccess(message);
                break;

            case MessageType.RANK_FAILURE:
                handleUserRankFailure(message);
                break;

            case MessageType.LEADERBOARD_SUCCESS:
                handleLeaderboardSuccess(message);
                break;

            case MessageType.LEADERBOARD_FAILURE:
                handleLeaderboardFailure(message);
                break;

            case MessageType.CHAT_SUCCESS:
                handleChatSuccess(message);
                break;

            case MessageType.CHAT_FAILURE:
                handleChatFailure(message);
                break;

            case MessageType.ADD_CHAT_SUCCESS:
                handleAddChatSuccess(message);
                break;

            case MessageType.ADD_CHAT_FAILURE:
                handleAddChatFailure(message);
                break;
            case MessageType.ONLINE_LIST:
                handleOnlineUsers(message);
                break;

            case MessageType.INVITE_RECEIVED:
                handleInviteReceived(message);
                break;

            case MessageType.INVITE_ACCEPT:
                handleInviteAccepted(message);
                break;

            case MessageType.INVITE_REJECT:
                handleInviteRejected(message);
                break;

            case MessageType.START_GAME:
//            case MessageType.GAME_START:
                handleStartGame(message);
                break;

            // Game messages forwarded to gameController if present
            case MessageType.SHOW_COLORS:
                // ensure game UI is visible
                if (gameController == null) {
                    Platform.runLater(this::showGameUI);
                    System.out.println("null");
                }
                if (gameController != null && message.getContent() instanceof List) {
                    System.out.println("!=null");
                    List<?> raw = (List<?>) message.getContent();
                    // try to cast to List<String>
                    try {
                        @SuppressWarnings("unchecked")
                        List<String> colors = (List<String>) raw;
                        gameController.onShowColors(colors);
                    } catch (ClassCastException ex) {
                        System.err.println("SHOW_COLORS content not List<String>");
                    }
                }
                break;

            case MessageType.GAME_TICK:
                if (gameController == null) {
                    Platform.runLater(this::showGameUI);
                }
                if (gameController != null && message.getContent() instanceof Integer) {
                    Integer sec = (Integer) message.getContent();
                    gameController.onGameTick(sec);
                }
                break;

            case MessageType.PICK_RESULT:
                if (gameController == null) {
                    Platform.runLater(this::showGameUI);
                }
                if (gameController != null && message.getContent() instanceof Object[]) {
                    Object[] arr = (Object[]) message.getContent();
                    try {
                        int row = (Integer) arr[0];
                        int col = (Integer) arr[1];
                        boolean hit = (Boolean) arr[2];
                        String marker = (String) arr[3];
                        int s1 = (Integer) arr[4];
                        int s2 = (Integer) arr[5];
                        gameController.onPickResult(row, col, hit, marker, s1, s2);
                    } catch (ClassCastException | ArrayIndexOutOfBoundsException ex) {
                        System.err.println("Invalid PICK_RESULT payload");
                    }
                }
                break;

            case MessageType.MATCH_RESULT:
                if (gameController == null) {
                    Platform.runLater(this::showGameUI);
                }
                if (gameController != null && message.getContent() instanceof Object[]) {
                    Object[] arr = (Object[]) message.getContent();
                    try {
                        int s1 = (Integer) arr[0];
                        int s2 = (Integer) arr[1];
                        String winner = (String) arr[2];
                        int a1 = (Integer) arr[3];
                        int a2 = (Integer) arr[4];
                        gameController.onGameEnd(s1, s2, winner, a1, a2);
                    } catch (ClassCastException | ArrayIndexOutOfBoundsException ex) {
                        System.err.println("Invalid MATCH_RESULT payload");
                    }
                }
                break;

            case MessageType.OPPONENT_QUIT:
                String quitterUsername = (String) message.getContent();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Đối thủ đã thoát");
                    alert.setHeaderText(null);
                    alert.setContentText("Người chơi " + quitterUsername + " đã thoát khỏi trận đấu.\nBạn thắng mặc định!");
                    alert.showAndWait();
                });
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
        this.user = (User) message.getContent();
        Platform.runLater(this::showMainUI);
    }

    private void handleLoginFailure(Message message) {
        String errorMsg = (String) message.getContent();
        Platform.runLater(() -> {
            System.out.println("Đăng nhập thất bại: " + errorMsg);
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

            this.mainController = loader.getController();
            if (this.mainController != null) {
                this.mainController.setClient(this);
            }

            stage.setScene(new Scene(root));
            stage.show();
            sendMessage(new Message(MessageType.CHAT, null));
            sendMessage(new Message(MessageType.ONLINE_LIST, null));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện chính.");
        }
    }

    public void showGameUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameView.fxml"));
            Parent root = loader.load();
            gameController = loader.getController();
            if (gameController != null) {
                gameController.setClient(this);
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện game.");
        }
    }
    //
    // Gửi lời mời
    public void sendInvite(String opponentName) {
        try {
            System.out.println("Gửi lời mời đến: " + opponentName);
            Message inviteMsg = new Message(MessageType.INVITE_REQUEST, opponentName);
            sendMessage(inviteMsg);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể gửi lời mời tới " + opponentName);
        }
    }

    public void showLeaderboardUI() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Leaderboard.fxml"));
            Parent root = loader.load();

            leaderboardController = loader.getController();
            if (leaderboardController != null) {
                leaderboardController.setClient(this);
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện leaderboard.");
        }
    }

    private void handleUserRankSuccess(Message message) {
        User user = (User) message.getContent();
        System.out.println("=== Received user from server ===");
        Platform.runLater(() -> {
            if(leaderboardController != null){
                leaderboardController.setUser(user);
//                leaderboardController.updateUserRank(user);
            }
            else{
                System.out.println("leaderboardController là giá trị null");
            }
        });
    }

    private void handleUserRankFailure(Message message) {
        String errorMsg = (String) message.getContent();
        Platform.runLater(() -> {
            System.out.println("Lấy rank thất bại: " + errorMsg);
        });
        // leaderboardController.showError(errorMsg);
    }

    private void handleLeaderboardSuccess(Message message) {
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) message.getContent();
        Platform.runLater(() -> {
            if (leaderboardController != null) {
                leaderboardController.updateLeaderboard(users);
            } else {
                System.out.println("leaderboardController là giá trị null");
            }
        });
    }

    private void handleLeaderboardFailure(Message message) {
        String errorMsg = (String) message.getContent();
        Platform.runLater(() -> {
            System.out.println("Lấy leaderboard thất bại: " + errorMsg);
        });
    }

    private void handleChatSuccess(Message message){
        List<Chat> chats = (List<Chat>) message.getContent();
        Platform.runLater(() -> {
            if (mainController != null){
                mainController.updateChat(chats);
            }
            else {
                System.out.println("mainController la null");
            }
        });
    }

    private void handleChatFailure(Message message){
        String errorMsg = (String) message.getContent();
        Platform.runLater(() -> {
            System.out.println("Lấy chat thất bại: " + errorMsg);
        });
    }

    private void handleAddChatSuccess(Message message){

    }

    private void handleAddChatFailure(Message message){
        String errorMsg = (String) message.getContent();
        Platform.runLater(() -> {
            System.out.println("Them chat thất bại: " + errorMsg);
        });
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

    // Khi nhận được lời mời từ người khác
    private void handleInviteReceived(Message message) {
        String fromUser = (String) message.getContent();
        System.out.println("Nhận được lời mời từ: " + fromUser);

        Platform.runLater(() -> {
            // if a result dialog is open in the game controller, close it so invite dialog is visible
            try {
                if (gameController != null) {
                    gameController.closeResultDialog();
                }
            } catch (Exception ignored) {}

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Lời mời thách đấu");
            alert.setHeaderText("Người chơi " + fromUser + " mời bạn chơi!");
            alert.setContentText("Bạn có muốn chấp nhận không?");
            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
            try {
                Message reply;
                if (result == ButtonType.OK) {
                    reply = new Message(MessageType.INVITE_ACCEPT, fromUser);
                    // Load the game UI immediately so SHOW_COLORS/GAME_TICK are handled
                    Platform.runLater(this::showGameUI);
                } else {
                    reply = new Message(MessageType.INVITE_REJECT, fromUser);
                }
                sendMessage(reply);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Khi đối thủ chấp nhận lời mời
    private void handleInviteAccepted(Message message) {
        String opponent = (String) message.getContent();
        Platform.runLater(() -> {
            showGameUI();
        });
    }

    // Khi đối thủ từ chối lời mời
    private void handleInviteRejected(Message message) {
        String opponent = (String) message.getContent();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lời mời bị từ chối");
            alert.setHeaderText(null);
            alert.setContentText("Người chơi " + opponent + " đã từ chối lời mời của bạn.");
            alert.showAndWait();
        });
    }

    // Khi server thông báo bắt đầu trận
    private void handleStartGame(Message message) {
        // payload expected: Object[]{ List<String> colors, String player1, String player2 }
        Object content = message.getContent();
        if (content instanceof Object[]) {
            Object[] arr = (Object[]) content;
            try {
                @SuppressWarnings("unchecked")
                List<String> colors = (List<String>) arr[0];
                String player1 = (String) arr[1];
                String player2 = (String) arr[2];
                String[][] board = (String[][]) arr[3];
                Platform.runLater(() -> {
                    // ensure UI loaded
                    showGameUI();
                    if (gameController != null) {
                        Executors.newSingleThreadScheduledExecutor().schedule(() ->
                                        Platform.runLater(() -> gameController.onSessionStart(colors, player1, player2,board)),
                                200, TimeUnit.MILLISECONDS
                        );
                    }
                });
            } catch (ClassCastException | ArrayIndexOutOfBoundsException ex) {
                System.err.println("Invalid START_GAME payload");
            }
        } else {
            // fallback: simple notification
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Trận đấu bắt đầu!");
                alert.setHeaderText(null);
                alert.setContentText("Trò chơi đang được khởi tạo...");
                alert.showAndWait();
            });
        }
    }

    //



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
