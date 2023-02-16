package client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Main {
    @Parameter(names={"--type", "-t"}, description="Type of request (get, set, delete, exit).", required=false)
    private static String type;
    @Parameter(names={"--key", "-k"}, description="Index of the element to get or set.", required=false)
    private static String key;
    @Parameter(names={"--value", "-v"}, description="Value to set.", required=false)
    private static String value;
    @Parameter(names={"-in"}, description="Input JSON-file to parse to server, in respective folder (REQUEST_FOLDER)." , required=false)
    private static String fileIn;
    private final static String ADDRESS = "127.0.0.1"; // localhost
    private final static int PORT = 12345;
    private final static String REQUEST_FOLDER = System.getProperty("user.dir") + "/src/client/data/";;

    public static void main(String ... argv) throws IOException {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(argv);
        main.run();
    }

    private void run() throws IOException {
        if ((type == null && fileIn == null) || (type != null && fileIn != null))
            throw new RuntimeException("You need to EITHER specify type of operation OR provide input file.");

        Files.createDirectories(Paths.get(REQUEST_FOLDER));
        Client client = new Client(ADDRESS, PORT);
        Request request = (fileIn != null) ?
                Request.FromFile(REQUEST_FOLDER + fileIn) :
                Request.FromTypeKeyValue(type, key, value);

        ClientRequestProxy clientRequestProxy = new ClientRequestProxy(client, request);
        clientRequestProxy.sendRequest();
        clientRequestProxy.getResponse();
        client.close();
    }
}
