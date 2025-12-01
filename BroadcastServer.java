import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BroadcastServer {
    private final int port;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private ServerSocket serverSocket;

    public BroadcastServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("[" + getTimestamp() + "] Starting Broadcast Server on port " + port);
        try {
            serverSocket = new ServerSocket(port);
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            System.out.println("[" + getTimestamp() + "] Server is ready and waiting for clients...");

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                    // Client ID will be logged after username is set
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.out.println("[" + getTimestamp() + "] Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[" + getTimestamp() + "] Server error: " + e.getMessage());
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        int sentCount = 0;
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
                sentCount++;
            }
        }
        if (sentCount > 0) {
            System.out.println("[" + getTimestamp() + "] Broadcasted message to " + sentCount + " client(s)");
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private void shutdown() {
        System.out.println("[" + getTimestamp() + "] Shutting down server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            System.out.println("[" + getTimestamp() + "] Server shutdown complete.");
        } catch (IOException e) {
            System.out.println("[" + getTimestamp() + "] Error during server shutdown: " + e.getMessage());
        }
    }

    private String getTimestamp() {
        return java.time.LocalTime.now().toString().substring(0, 12);
    }

    public int getClientCount() {
        return clients.size();
    }

    public void listClients(ClientHandler requester) {
        int count = 0;
        for (ClientHandler client : clients) {
            if (client != requester) {
                requester.sendMessage("  - " + client.getUsername());
                count++;
            }
        }
        if (count == 0) {
            requester.sendMessage("  (No other users connected)");
        }
    }
}
