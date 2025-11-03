package server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import constant.MessageType;
import constant.Status;
import model.Chat;
import model.Message;
import model.User;
import server.controller.ChatController;
import server.controller.UserController;

/**
 * ClientHandler là lớp này đại diện cho mỗi client kết nối tới server, mỗi client (user) khi dùng sẽ có 1 clientHandler
 * => bao gồm server và socket, clientManager để biết clientHandler thuộc về clientManager nào
 * có đầu vào, đầu ra và User tương ứng với clientHandle
 */
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
    private final ChatController chatController = new ChatController();
    /*
     * khởi tạo khi có client kết nối tới server
     */
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
            // Khi còn chạy
            while (isRunning) {
                System.out.println("Đang chờ message...");
                // đọc message từ client
                Message message = (Message) in.readObject();
                System.out.println("Nhận được message: " + (message != null ? message.getType() : "null"));
                // xử lý message
                if (message != null) {
                    handleMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException  e) {
            System.out.println("=== LỖI CHI TIẾT ===");
            System.out.println("Thông báo lỗi: " + e.getMessage());
            isRunning = false;
        } finally {
            try {
                if (user != null) {
                    try {
                        clientManager.removeClient(this);
                        System.out.println("ClientHandler: removed client " + user.getUsername());
                    } catch (Exception ex) {
                        System.err.println("Error removing client on disconnect: " + ex.getMessage());
                    }
                }
                // notify game manager (if any) that this client disconnected
                try {
                    if (server != null && server.getGameManager() != null) {
                        server.getGameManager().handleExit(this);
                    }
                } catch (Exception ex) {
                    // ignore
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
                    System.out.println("--- Xử lý login ---");
                    handleLogin(message);
                    break;

                case MessageType.LOGOUT:
                    System.out.println("--- Xử lý logout ---");
                    handleLogout();
                    break;

                case MessageType.ONLINE_LIST:
                    System.out.println("--- Xử lý danh sách online ---");
                    handleGetOnlineUsers();
                    break;

                case MessageType.INVITE_REQUEST:
                    // content: String opponentUsername
                    try {
                        String targetName = (String) message.getContent();
                        if (targetName != null && user != null) {
                            ClientHandler target = clientManager.getClientByUsername(targetName);
                            if (target != null) {
                                // forward invite to target
                                System.out.println("Forwarding INVITE_REQUEST from " + user.getUsername() + " to " + targetName);
                                target.sendResponse(new Message(MessageType.INVITE_RECEIVED, user.getUsername()));
                            } else {
                                // target not online
                                sendResponse(new Message(MessageType.INVITE_REJECT, "Target not online"));
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Invalid INVITE_REQUEST payload");
                    }
                    break;

                case MessageType.INVITE_ACCEPT:
                    try {
                        String inviter = (String) message.getContent();
                        if (inviter != null && user != null) {
                            ClientHandler inviterHandler = clientManager.getClientByUsername(inviter);
                            if (inviterHandler != null) {
                                inviterHandler.sendResponse(new Message(MessageType.INVITE_ACCEPT, user.getUsername()));
                                System.out.println("Creating game session between " + inviter + " and " + user.getUsername());
                                server.getGameManager().createSession(inviterHandler, this);
                                clientManager.updateStatus(inviter, Status.NOT_AVAILABLE);
                                clientManager.updateStatus(user.getUsername(), Status.NOT_AVAILABLE);
                            } else {
                                sendResponse(new Message(MessageType.INVITE_REJECT, "Inviter not online"));
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Invalid INVITE_ACCEPT payload");
                    }
                    break;

                case MessageType.INVITE_REJECT:
                    // content: String inviterUsername
                    try {
                        String inviter = (String) message.getContent();
                        if (inviter != null && user != null) {
                            ClientHandler inviterHandler = clientManager.getClientByUsername(inviter);
                            if (inviterHandler != null) {
                                System.out.println("INVITE_REJECT from " + user.getUsername() + " to inviter=" + inviter);
                                inviterHandler.sendResponse(new Message(MessageType.INVITE_REJECT, user.getUsername()));
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Invalid INVITE_REJECT payload");
                    }
                    break;


                case MessageType.PICK_CELL:
                    // content: int[]{row,col}
                    try {
                        int[] rc = (int[]) message.getContent();
                        server.getGameManager().handlePick(this, rc[0], rc[1]);
                    } catch (Exception ex) {
                        System.err.println("Invalid PICK_CELL payload");
                    }
                    break;

                case MessageType.EXIT_GAME:
                    // a player wants to exit current game
                    server.getGameManager().handleExit(this);
                    break;

                case MessageType.LEADERBOARD:
                    handleGetLeaderboard();
                    break;

                case MessageType.RANK:
                    handleGetRank(message);
                    break;

                case MessageType.ADD_CHAT:
                    handleAddChat(message);
                    break;

                case MessageType.CHAT:
                    handleGetChat();
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
                if(clientManager.isUserOnline(authenticatedUser.getUsername())) {
                    sendResponse(new Message(MessageType.LOGIN_FAILURE, "User đã online ở thiết bị khác!"));
                    return;
                }
                authenticatedUser.setStatus(Status.AVAILABLE);
                this.user = authenticatedUser;
                clientManager.addClient(this);
                sendResponse(new Message(MessageType.LOGIN_SUCCESS, authenticatedUser));
            } else {
                sendResponse(new Message(MessageType.LOGIN_FAILURE, "Sai username hoặc password"));
            }
        } catch (Exception e) {;
            sendResponse(new Message(MessageType.LOGIN_FAILURE, "Server error"));
        }
    }

    private void handleLogout()  throws IOException {
        if(user != null) {
            clientManager.removeClient(this);
            if (socket != null && !socket.isClosed()) {
                sendResponse(new Message(MessageType.LOGOUT_SUCCESS, "Đăng xuất thành công."));
            }
            isRunning = false;
            socket.close();
        }
    }

    private void handleAddChat(Message message){
        try{
            Chat chat = (Chat) message.getContent();
            // Ensure the message's user is the authenticated user on this connection
            chat.setUser(this.user);
            boolean isAdded = chatController.saveChat(chat);
            if(isAdded){
                sendResponse(new Message(MessageType.ADD_CHAT_SUCCESS, "Them chat thanh cong"));
                // After successfully adding, broadcast refreshed chat list
                List<Chat> chats = chatController.getAllChat();
                clientManager.broadcast(new Message(MessageType.CHAT_SUCCESS, chats));
            }
            else {
                sendResponse(new Message(MessageType.ADD_CHAT_FAILURE, "Them chat that bai"));
            }
        } catch (Exception e) {
            sendResponse(new Message(MessageType.ADD_CHAT_FAILURE, "Server error"));
            e.printStackTrace();
        }
    }

    private void handleGetChat(){
        try{
            List<Chat> chats = chatController.getAllChat();
            if(!chats.isEmpty()){
                sendResponse(new Message(MessageType.CHAT_SUCCESS, chats));
            }
            else{
                sendResponse(new Message(MessageType.CHAT_FAILURE, "No chat found."));
            }
        } catch (Exception e){
            sendResponse(new Message(MessageType.CHAT_FAILURE, "Server error"));
            e.printStackTrace();
        }
    }

    private void handleGetOnlineUsers() {
        List<User> users = clientManager.getOnlineUsers();
        sendResponse(new Message(MessageType.ONLINE_LIST, users));
    }

    private void handleGetLeaderboard(){
        try{
            List<User> users = userController.getLeaderboard();
            for(User user : users){
                System.out.println(user.getUsername());
            }
            if (!users.isEmpty()){
                sendResponse(new Message(MessageType.LEADERBOARD_SUCCESS, users));
                System.out.println("Gửi yêu cầu lấy bảng xếp hạng thành công");
            } else {
                sendResponse(new Message(MessageType.LEADERBOARD_FAILURE, "Không có user nào"));
                System.out.println("Không có user nào trong CSDL");
            }
        } catch(Exception e){
            e.printStackTrace();
            sendResponse(new Message(MessageType.LEADERBOARD_FAILURE, "Server error"));
            System.out.println("Lỗi Server khi gửi yêu cầu lấy BXH");
        }
    }

    public void handleGetRank(Message message){
        try{
            String username = (String) message.getContent();
            User user = userController.getRankByUsername(username);
            if(user != null){
                sendResponse(new Message(MessageType.RANK_SUCCESS, user));
            }
            else {
                sendResponse(new Message(MessageType.RANK_FAILURE, "User not found"));
            }
            System.out.println("Gửi yêu cầu lấy xếp hạng cá nhân thành công");
        } catch(Exception e){
            e.printStackTrace();
            sendResponse(new Message(MessageType.RANK_FAILURE, "Server error"));
            System.out.println("Lỗi Server khi gửi yêu cầu lấy XH cá nhân");
        }
    }


    public void sendResponse(Message response) {
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket đã đóng: " + (user != null ? user.getUsername() : "client"));
            return;
        }

        try {
            out.reset();
            out.writeObject(response);
            out.flush();
        } catch (IOException e) {
            System.out.println("Lỗi khi gửi phản hồi tới " + (user != null ? user.getUsername() : "client") + ": " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public User getUser(){return this.user;}
    
    public ClientManager getClientManager() {
        return this.clientManager;
    }
}

