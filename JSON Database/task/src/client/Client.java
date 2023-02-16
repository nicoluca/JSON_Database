package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public Client(String address, int port) throws IOException {
        this.socket = new Socket(InetAddress.getByName(address), port);
        System.out.println("Client started!");
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public void close() throws IOException {
        this.socket.close();
        this.input.close();
        this.output.close();
    }

    public String receiveUTF() throws IOException {
        return this.input.readUTF();
    }

    public void sendUTFRequest(String request) throws IOException {
        this.output.writeUTF(request);
        this.output.flush();
    }
}
