package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Message;
import model.User;
import server.controller.UserController;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private RunServer server;
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
            System.out.println("=== vào try catch ===");
            while (isRunning) {
                System.out.println("Đang chờ message...");
                Message message = (Message) in.readObject();
                System.out.println("Nhận được message: " + (message != null ? message.getType() : "null"));
                if (message != null) {
                    handleMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException  e) {
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
                case "login":
                    System.out.println("=== XỬ LÝ LOGIN ===");
                    handleLogin(message);
                    System.out.println("=== LOGIN XONG ===");
                    break;
            }
        } catch (Exception e) {
            System.out.println("=== LỖI TRONG handleMessage ===");
            e.printStackTrace();
        }
    }

    private void handleLogin(Message message) {
        try {
            User loginInfo = (User) message.getContent();
            User authenticatedUser = userController.login(loginInfo);

            if (authenticatedUser != null) {
                this.user = authenticatedUser;
                System.out.println("LOGIN SUCCESS with User: " + authenticatedUser.getUsername());
                Message response = new Message("login_success", authenticatedUser);
                out.writeObject(response);
                out.flush();

            } else {
                Message response = new Message("login_fail", "Sai username hoặc password");
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("=== EXCEPTION trong handleLogin ===");
            System.out.println("Exception type: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            try {
                out.writeObject(new Message("login_fail", "Server error"));
                out.flush();
            } catch (Exception ex) {
                System.out.println("Không thể gửi error response: " + ex.getMessage());
            }
        }
    }
}
