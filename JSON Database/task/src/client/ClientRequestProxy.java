package client;

import java.io.IOException;

public class ClientRequestProxy {
    private final Client client;
    private final Request request;

    public ClientRequestProxy(Client client, Request request) {
        this.client = client;
        this.request = request;
    }

    public void getResponse() throws IOException {
        System.out.println("Client received: " + this.client.receiveUTF());
    }

    public void sendRequest() throws IOException {
        this.client.sendUTFRequest(this.request.toJson());
        System.out.println("Client sent: " + this.request.toJson());
    }

}
