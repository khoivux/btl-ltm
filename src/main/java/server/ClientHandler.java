package server;

import model.Message;
import model.User;
import server.dao.UserDAO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private DBManager dbManager;
    private volatile boolean isRunning = true;
    // Object
    private User user;


    public ClientHandler(Socket socket, Server server, DBManager dbManager) {
        this.socket = socket;
        this.server = server;
        this.dbManager = dbManager;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Message message = (Message) in.readObject();
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
        switch (message.getType()) {
            case "login":
                handleLogin(message);
                break;
            case "register":
                //
                break;
        }
    }

    /*
    Đăng nhập
     */
    private void handleLogin(Message message) {
        User loginInfo = (User) message.getContent();
        User authenticatedUser = dbManager.authenticateUser(loginInfo);
        try {
            if (authenticatedUser != null) {
                this.user = authenticatedUser;
                System.out.println("User đăng nhập thành công: " + user.getUsername());
            }
            else {
                // sai username/password
                out.writeObject(new Message("login_fail", "Sai username hoặc password"));
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
