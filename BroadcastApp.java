public class BroadcastApp {
    private static final int DEFAULT_PORT = 5005;
    private static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String command = args[0];
        switch (command) {
            case "start":
                int port = parsePort(args, DEFAULT_PORT);
                new BroadcastServer(port).start();
                break;
            case "connect":
                String host = parseHost(args, DEFAULT_HOST);
                port = parsePort(args, DEFAULT_PORT);
                new BroadcastClient(host, port).start();
                break;
            case "help":
            case "--help":
            case "-h":
                printUsage();
                break;
            default:
                System.out.println("Invalid command: " + command);
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Java Broadcast Server");
        System.out.println("=====================");
        System.out.println("Usage:");
        System.out.println("  Start server:  java BroadcastApp start [--port PORT]");
        System.out.println("  Connect client: java BroadcastApp connect [--host HOST] [--port PORT]");
        System.out.println("  Show help:     java BroadcastApp help");
        System.out.println();
        System.out.println("Defaults: host=localhost, port=5005");
        System.out.println("Example: java BroadcastApp connect --host 192.168.1.100 --port 8080");
    }

    private static String parseHost(String[] args, String defaultHost) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--host".equals(args[i]) || "-h".equals(args[i])) {
                return args[i + 1];
            }
        }
        return defaultHost;
    }

    private static int parsePort(String[] args, int defaultPort) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--port".equals(args[i]) || "-p".equals(args[i])) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port number: " + args[i + 1] + ". Using default: " + defaultPort);
                    return defaultPort;
                }
            }
        }
        return defaultPort;
    }
}
