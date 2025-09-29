package server;

import model.Message;
import model.User;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientManager {
    private final ConcurrentHashMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();

    public ClientManager() {
    }

    public synchronized  void addClient(ClientHandler clientHandler) {
        clientMap.put(clientHandler.getUser().getUsername(), clientHandler);
        System.out.println("Client added: " + clientHandler.getUser().getUsername());
    }

    public ClientHandler getClientByUsername(String username) {
        return clientMap.get(username);
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        if(clientHandler.getUser() != null) {
            clientMap.remove(clientHandler.getUser().getUsername());
            System.out.println("Client removed: " + clientHandler.getUser().getUsername());
        }
    }

    public synchronized void broadcast(Message message) {
        System.out.println("Broadcasting message to " + clientMap.size() + " clients: " + message.getType().toString());
        for (ClientHandler client : clientMap.values()) {
            try {
                client.sendResponse(message);
            } catch (Exception e) {
                System.err.println("Failed to send message to client: " + e.getMessage());
            }
        }
    }

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
     */
    public synchronized List<User> getOnlineUsers() {
        return clientMap.values().stream()
                .filter(client -> client.getUser() != null)
                .map(ClientHandler::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra user có đang online không
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
}
