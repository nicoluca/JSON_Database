package server;

import client.Request;
import client.RequestDeserializer;
import client.RequestType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Singleton class that handles incoming client requests.
 */

public class ClientHandler {
    private static ClientHandler clientHandlerInstance = null;
    private final ServerSocket server;
    private final DB db;
    private final ReadWriteLock readWriteLock;
    private final Lock readLock;
    private final Lock writeLock;
    private int poolSize;
    private boolean shouldShutDown = false;


    private ClientHandler(ServerSocket server, DB db, int poolSize) {
        this.server = server;
        this.db = db;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
        this.poolSize = poolSize;
    }

    public static void startClientHandlerInstance(ServerSocket server, DB db, int poolSize) {
        if (clientHandlerInstance == null)
            clientHandlerInstance = new ClientHandler(server, db, poolSize);
        else {
            System.out.println("ClientHandler instance already running!");
        }
    }

    public static ClientHandler getClientHandlerInstance() {
        if (clientHandlerInstance == null)
            throw new RuntimeException("ClientHandler instance not running!");
        return clientHandlerInstance;
    }


    public void handleIncomingRequests() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(this.poolSize);
        System.out.println("Executor service started with " + this.poolSize + " threads");

        while (!shouldShutDown) {
            Socket client = this.server.accept();
            System.out.println("New client connected: "
                    + client.getInetAddress()
                    .getHostAddress());

            SingleClientHandler singleClientHandler = new SingleClientHandler(client);
            shouldShutDown = executeClientRequest(singleClientHandler, executor);
        }

        executor.shutdown();
        System.out.println("Executor service shut down");
    }

    private boolean executeClientRequest(SingleClientHandler singleClientHandler, ExecutorService executor) {
        Future<?> future = executor.submit(singleClientHandler);
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        future.cancel(true);
        return singleClientHandler.isServerShutDown();
    }

    // Get response json from DB, synchronized by request type
    private String getResponseJson(Request request) {
        assert Arrays.asList("get", "set", "delete", "exit").contains(request.getType().toString().toLowerCase())
                : "Request type must be get, set, or delete!";

        Response response;
        switch (request.getType()) {
            case GET:
                synchronized (readLock) {
                    response = DbRequestHandler.handleAnyRequest(request, this.db);
                    break;
                }
            default:
                synchronized (writeLock) {
                    response = DbRequestHandler.handleAnyRequest(request, this.db);
                    break;
                }
        }
        return response.toJson();
    }

    private class SingleClientHandler extends Thread {
        private Socket client;
        public SingleClientHandler(Socket client) { this.client = client; }
        private boolean serverShutDown = false;

        @Override
        public void run() {
            try (DataInputStream inputStream = new DataInputStream(client.getInputStream());
                 DataOutputStream outputStream = new DataOutputStream(client.getOutputStream())) {
                String threadName = Thread.currentThread().getName();
                System.out.println(String.format("%s is handling client from %s",
                        threadName,
                        client.getInetAddress().getHostAddress()));

                String jsonInput = inputStream.readUTF();
                System.out.println("Server received: " + jsonInput);
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Request.class, new RequestDeserializer()).create();
                Request request = gson.fromJson(jsonInput, Request.class);

                String jsonOutput = getResponseJson(request);
                if (request.getType() == RequestType.EXIT)
                    synchronized (writeLock) {
                        serverShutDown = true;
                    }

                outputStream.writeUTF(jsonOutput);
                System.out.println("Server sent: " + jsonOutput);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    client.close();
                    System.out.println("Client disconnected: "
                            + client.getInetAddress()
                            .getHostAddress());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public boolean isServerShutDown() {
                return serverShutDown;
        }
    }

}
