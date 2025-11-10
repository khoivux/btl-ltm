package server;

import server.manager.ClientManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RunServer {
    private static final int PORT = 23456;
    private ServerSocket serverSocket;
    private ClientManager clientManager;
    private server.controller.GameManager gameManager;

    public RunServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            clientManager = new ClientManager();
            gameManager = new server.controller.GameManager();
            System.out.println("Server đã khởi động trên cổng " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("=== CLIENT ĐÃ KẾT NỐI: " + clientSocket.getRemoteSocketAddress() + " ===");
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                Thread handlerThread = new Thread(clientHandler);
                handlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientManager getClientManager() {
        return clientManager;
    }

    public server.controller.GameManager getGameManager() {
        return gameManager;
    }

    public static void main(String[] args) {
        RunServer server = new RunServer();
        server.start();
    }
}
