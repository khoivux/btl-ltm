package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RunServer {
    private static final int PORT = 23456;
    private ServerSocket serverSocket;

    public RunServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server đã khởi động trên cổng " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        System.out.println("=== SERVER BẮT ĐẦU LẮNG NGHE ===");
        try {
            while (true) {
                System.out.println("Đang chờ client kết nối...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("=== CLIENT ĐÃ KẾT NỐI: " + clientSocket.getRemoteSocketAddress() + " ===");

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                Thread handlerThread = new Thread(clientHandler);
                handlerThread.start();
                System.out.println("=== ĐÃ START ClientHandler THREAD ===");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        RunServer server = new RunServer();
        server.start();
    }
}
