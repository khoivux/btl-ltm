package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import constant.MessageType;
import model.Message;
import model.User;
import server.controller.UserController;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private RunServer server;
    private ClientManager clientManager;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean isRunning = true;
    // Object
    private User user;
    // Controller
    private final UserController userController = new UserController();


    public ClientHandler(Socket socket, RunServer server) {
        this.socket = socket;
        this.server = server;
        this.clientManager = server.getClientManager();
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("=== ClientHandler TẠO THÀNH CÔNG ===");
        } catch (IOException e) {
            System.out.println("=== LỖI TẠO ClientHandler ===");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("=== ClientHandler THREAD BẮT ĐẦU CHẠY ===");
        try {
            while (isRunning) {
                System.out.println("Đang chờ message...");
                Message message = (Message) in.readObject();
                System.out.println("Nhận được message: " + (message != null ? message.getType() : "null"));
                if (message != null) {
                    handleMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException  e) {
            System.out.println("=== LỖI CHI TIẾT ===");
            System.out.println("Loại lỗi: " + e.getClass().getSimpleName());
            System.out.println("Thông báo lỗi: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Kết nối với " + (user != null ? user.getUsername() : "client") + " bị ngắt.");
            isRunning = false;
        } finally {
            try {
                if (user != null) {
//                    try {
//                        dbManager.updateUserStatus(user.getId(), "offline");
//                    } catch (SQLException ex) {
//                        System.err.println("Không thể cập nhật trạng thái user: " + ex.getMessage());
//                    }
//                    server.broadcast(new Message("status_update",
//                            user.getUsername() + " đã offline."));
//                    server.removeClient(this);
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(Message message) {
        try {
            switch (message.getType()) {

                case MessageType.REGISTER:
                    handleRegister(message);
                    break;

                case MessageType.LOGIN:
                    handleLogin(message);
                    break;

                case MessageType.LOGOUT:
                    handleLogout();
                    break;

                case MessageType.ONLINE_LIST:
                    handleGetOnlineUsers();
                    break;

                default:
                    System.out.println("ERROR: Message không hợp lệ ");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleRegister(Message message) {
        try {
            userController.register((User) message.getContent());
            sendResponse(new Message(MessageType.REGISTER_SUCCESS, null));
        } catch(SQLIntegrityConstraintViolationException e) {
            sendResponse(new Message(MessageType.REGISTER_FAILURE, "Username đã tồn tại!"));
        } catch (Exception e) {
            sendResponse(new Message(MessageType.REGISTER_FAILURE, "Đăng ký thất bại!"));
        }
    }

    private void handleLogin(Message message) {
        try {
            User authenticatedUser = userController.login((User) message.getContent());
            if (authenticatedUser != null) {
                this.user = authenticatedUser;
                clientManager.addClient(this);
                clientManager.broadcast(new Message(MessageType.UPDATE_USER_STATUS, authenticatedUser.getUsername() + "đã online"));
                sendResponse(new Message(MessageType.LOGIN_SUCCESS, authenticatedUser));
                System.out.println("Login thành công với username: " + authenticatedUser.getUsername());
            } else {
                sendResponse(new Message(MessageType.LOGIN_FAILURE, "Sai username hoặc password"));
            }
        } catch (Exception e) {;
            sendResponse(new Message(MessageType.LOGIN_FAILURE, "Server error"));
        }
    }

    private void handleLogout()  throws IOException {
        if(user != null) {
            clientManager.broadcast(new Message(MessageType.UPDATE_USER_STATUS, user.getUsername() + " đã offline"));
            if (socket != null && !socket.isClosed()) {
                sendResponse(new Message(MessageType.LOGOUT_SUCCESS, "Đăng xuất thành công."));
            }
            isRunning = false;
            clientManager.removeClient(this);
            socket.close();
        }
    }

    public void handleGetOnlineUsers() {
        List<User> users = clientManager.getOnlineUsers();
        sendResponse(new Message(MessageType.ONLINE_LIST, users));
    }


    public void sendResponse(Message response) {
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket đã đóng: " + (user != null ? user.getUsername() : "client"));
            return;
        }

        try {
            out.writeObject(response);
            out.flush();
        } catch (IOException e) {
            System.out.println("Lỗi khi gửi phản hồi tới " + (user != null ? user.getUsername() : "client") + ": " + e.getMessage());
            try {
                socket.close(); // đánh dấu client ngắt kết nối
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public User getUser(){return this.user;}
}
