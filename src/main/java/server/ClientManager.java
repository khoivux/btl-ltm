package server;

import constant.MessageType;
import constant.Status;
import model.Message;
import model.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// lớp này quản lý tất cả các clientHandler
public class ClientManager {
    private final ConcurrentHashMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();

    public ClientManager() {
    }

    // Thêm client :  map username với clientHandler rồi cho thêm (hoặc ghi đè vào clientMap)
    public synchronized  void addClient(ClientHandler clientHandler) {
        clientMap.put(clientHandler.getUser().getUsername(), clientHandler);
        broadcastExcept(new Message(MessageType.UPDATE_USER_STATUS, clientHandler.getUser()), clientHandler);
        System.out.println("Client added: " + clientHandler.getUser().getUsername());
    }

    // Lấy client theo username => dựa vào map lấy clientHandler
    public ClientHandler getClientByUsername(String username) {
        return clientMap.get(username);
    }

    // Xóa clientHandler ra khỏi map: mỗi lần logout thì xóa clientHandler ra khỏi map
    // Vì clientManager quản lý tất cả các clientHandler nên xóa cặp (username, clientHandler)
    // Xóa theo username
    public synchronized void removeClient(ClientHandler clientHandler) {
        if(clientHandler.getUser() != null) {
            User logoutInfo = new User(clientHandler.getUser());
            logoutInfo.setStatus(Status.OFFLINE);
            broadcast(new Message(MessageType.UPDATE_USER_STATUS, logoutInfo));
        }
    }

    // Phát sóng một message tới tất cả các clientHandler => cập nhật thông tin realtime
    // Duyệt qua các clientHandler hiện tại rồi gửi response đến client
    public synchronized void broadcast(Message message) {
        for (ClientHandler client : clientMap.values()) {
            try {
                client.sendResponse(message);
            } catch (Exception e) {
                System.err.println("Failed to send message to client: " + e.getMessage());
            }
        }
    }

    // Broadcast đến tất cả client ngoại trừ 1 client (thường là client vừa hành động)
    public synchronized void broadcastExcept(Message message, ClientHandler excludeClient) {
        for (ClientHandler client : clientMap.values()) {
            if (client != excludeClient) {
                try {
                    client.sendResponse(message);
                } catch (Exception e) {
                    System.err.println("Failed to send message to client: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Lấy danh sách tất cả client đang kết nối
     */
    public synchronized List<ClientHandler> getAllClients() {
        return clientMap.values().stream().toList();
    }

    /**
     * Lấy danh sách user đang online
     * Duyệt qua các clientHandler hiện tại (dùng stream để kết hợp với filter và map)
     * mỗi một clientHandler có user => lấy user đó ra rồi collect thành list
     */
    public synchronized List<User> getOnlineUsers() {
        return clientMap.values().stream()
                .filter(client -> client.getUser() != null)
                .map(ClientHandler::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra user có đang online không
     * Nếu trong clientMap có key = username => user đó đang online
     */
    public boolean isUserOnline(String username) {
        return clientMap.containsKey(username);
    }

    /**
     * Lấy số lượng client đang kết nối
     */
    public int getClientCount() {
        return clientMap.size();
    }

    public synchronized void disconnectAllClients() {
        System.out.println("Disconnecting all " + clientMap.size() + " clients");
        clientMap.clear();
    }

    public synchronized void updateStatus(String username, Status status) {
        ClientHandler clientHandler = getClientByUsername(username);
        if (clientHandler != null && clientHandler.getUser() != null) {
            clientHandler.getUser().setStatus(status);
            broadcast(new Message(MessageType.UPDATE_USER_STATUS, clientHandler.getUser()));
        }
    }
}
