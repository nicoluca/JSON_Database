package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Main {
    private final static String ADDRESS = "127.0.0.1";
    private final static int PORT = 12345;
    private final static String DB_FOLDER = System.getProperty("user.dir") + "/src/server/data/";
    private final static String DB_FILE = "db.json";
    private final static int POOL_SIZE = 4;

    public static void main(String[] args) throws IOException {
        DB.startNewDb(DB_FOLDER, DB_FILE);

        try (ServerSocket server =
                     new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            System.out.println("Server has started!");
            ClientHandler.startClientHandlerInstance(server, DB.getDb(), POOL_SIZE);
            ClientHandler.getClientHandlerInstance().handleIncomingRequests();
        }

        System.out.println("Server has stopped!");
    }
}
