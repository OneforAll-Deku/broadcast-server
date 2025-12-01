import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BroadcastServer server;
    private PrintWriter out;
    private BufferedReader in;
    private final String clientId;
    private String username;

    public ClientHandler(Socket socket, BroadcastServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Request username
            out.println("Please enter your username:");
            String firstMessage = in.readLine();
            if (firstMessage == null || firstMessage.trim().isEmpty()) {
                username = "User" + clientId.split(":")[1];
            } else if (firstMessage.startsWith("/name ")) {
                username = firstMessage.substring(6).trim();
                if (username.isEmpty()) {
                    username = "User" + clientId.split(":")[1];
                }
            } else {
                username = firstMessage.trim();
            }
            
            // Send welcome message
            String welcomeMsg = "Welcome to the broadcast server, " + username + "!";
            out.println(welcomeMsg);
            out.println("There are " + (server.getClientCount() - 1) + " other client(s) online.");
            out.println("Type '/help' for available commands.");

            // Notify others of new user
            server.broadcast("[" + getTimestamp() + "] " + username + " joined the server", this);
            System.out.println("[" + getTimestamp() + "] Client " + clientId + " connected as: " + username);

            String message;
            while ((message = in.readLine()) != null) {
                if ("exit".equalsIgnoreCase(message) || "/quit".equalsIgnoreCase(message)) {
                    System.out.println("[" + getTimestamp() + "] Client " + username + " (" + clientId + ") requested to exit.");
                    server.broadcast("[" + getTimestamp() + "] " + username + " left the server", this);
                    break;
                }
                if (message.trim().isEmpty()) {
                    continue;
                }
                
                // Handle special commands
                if (message.startsWith("/")) {
                    handleCommand(message);
                    continue;
                }
                
                System.out.println("[" + getTimestamp() + "] " + username + " (" + clientId + "): " + message);
                server.broadcast("[" + getTimestamp() + "] " + username + ": " + message, this);
            }
        } catch (IOException e) {
            System.out.println("[" + getTimestamp() + "] Error handling client " + clientId + ": " + e.getMessage());
        } finally {
            close();
        }
    }

    private void handleCommand(String command) {
        if (command.equalsIgnoreCase("/help")) {
            out.println("Available commands:");
            out.println("  /help  - Show this help message");
            out.println("  /list  - List all connected users");
            out.println("  /quit  - Disconnect from server");
        } else if (command.equalsIgnoreCase("/list")) {
            out.println("Connected users (" + server.getClientCount() + "):");
            server.listClients(this);
        } else if (command.startsWith("/name ")) {
            String newName = command.substring(6).trim();
            if (!newName.isEmpty()) {
                String oldName = username;
                username = newName;
                out.println("Username changed to: " + username);
                server.broadcast("[" + getTimestamp() + "] " + oldName + " is now known as " + username, this);
            }
        } else {
            out.println("Unknown command: " + command + ". Type /help for available commands.");
        }
    }

    private String getTimestamp() {
        return java.time.LocalTime.now().toString().substring(0, 12);
    }

    public void sendMessage(String message) {
        if (out != null && message != null) {
            out.println(message);
        }
    }

    public void close() {
        try {
            server.removeClient(this);
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            String displayName = username != null ? username : clientId;
            System.out.println("[" + getTimestamp() + "] Client disconnected: " + displayName + " (" + clientId + ") (Remaining clients: " + server.getClientCount() + ")");
        } catch (IOException e) {
            System.out.println("[" + getTimestamp() + "] Error closing client connection " + clientId + ": " + e.getMessage());
        }
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username != null ? username : clientId;
    }
}
