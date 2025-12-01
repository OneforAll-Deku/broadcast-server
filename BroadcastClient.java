import java.io.*;
import java.net.*;

public class BroadcastClient {
    private final String host;
    private final int port;

    public BroadcastClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        System.out.println("Connecting to Broadcast Server on " + host + ":" + port);
        try (
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("✓ Connected to server successfully!");
            System.out.println("Enter your username when prompted, or type messages to broadcast.");
            System.out.println("Type '/help' for commands or 'exit' to quit.\n");
            
            // Thread for receiving messages
            Thread receiveThread = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(">>> " + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("\n[Connection lost] Disconnected from server.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Main thread for sending messages
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if ("exit".equalsIgnoreCase(userInput)) {
                    out.println("exit");
                    System.out.println("Disconnecting...");
                    break;
                }
                if (userInput.trim().isEmpty()) {
                    System.out.print("> "); // Prompt for next input
                    continue;
                }
                out.println(userInput);
            }
        } catch (ConnectException e) {
            System.out.println("✗ Connection failed: Could not connect to server at " + host + ":" + port);
            System.out.println("  Make sure the server is running!");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
        System.out.println("Client disconnected.");
    }
}
