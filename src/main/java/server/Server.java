package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 23456;
    private ServerSocket serverSocket;
    private DBManager dbManager;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            dbManager = new DBManager();
            System.out.println("Server đã khởi động trên cổng " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        try {
            while (true) {
                // Chấp nhận kết nối từ client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client kết nối: " + clientSocket);

                // Tạo ClientHandler để xử lý client này
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, dbManager);
                // Chạy ClientHandler trong thread riêng
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
