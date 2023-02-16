package client;

import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

public class Request {
    private final RequestType type;
    private final String[] key;
    private final JsonElement value;
    private boolean parseKeyAsArray;

     Request(RequestType type, String[] key, JsonElement value, boolean parseKeyAsArray) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.parseKeyAsArray = parseKeyAsArray;
    }

    public static Request FromTypeKeyValue(String type, String key, String value) {
        if (key == null)
            return new Request(RequestType.fromString(type), null, null, false);
        if (value == null)
            return new Request(RequestType.fromString(type), new String[] {key}, null, false);
        else
            return new Request(RequestType.fromString(type), new String[] {key}, jsonElementFromString(value), false);
    }

    private static JsonElement jsonElementFromString(String value) {
        String quotedValue = "\"" + value + "\""; // Wrap in quotes so string gets read as single string when put in from console
        JsonElement jsonElement = new JsonParser().parse(quotedValue);
        return jsonElement;
    }

    public static Request FromFile(String fileIn) throws FileNotFoundException {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(Paths.get(fileIn).toFile()));

        if (!jsonElement.isJsonObject())
            throw new RuntimeException("Input file is not a valid JSON object.");
        if (!jsonElement.getAsJsonObject().has("type"))
            throw new RuntimeException("Input file does not have a 'type' field.");

        if (jsonElement.getAsJsonObject().get("type").getAsString().equals("exit"))
            return new Request(RequestType.EXIT, null, null, true);

        JsonElement keyElement = jsonElement.getAsJsonObject().get("key");
        switch (jsonElement.getAsJsonObject().get("type").getAsString()) {
            case "get":
                return new Request(RequestType.GET,
                        parseKey(keyElement),
                        null, isKeyArray(keyElement));
            case "set":
                return new Request(RequestType.SET,
                        parseKey(keyElement),
                        jsonElement.getAsJsonObject().get("value"), isKeyArray(keyElement));
            case "delete":
                return new Request(RequestType.DELETE,
                        parseKey(keyElement),
                        null, isKeyArray(keyElement));
            default:
                throw new RuntimeException("File is not a valid request.");
        }
    }

    private static String[] parseKey(JsonElement key) {
        if (key.isJsonArray())
            return new Gson().fromJson(key, String[].class);
        else if (key.isJsonPrimitive())
            return new String[] {key.getAsString()};
        else
            throw new RuntimeException("Key is not a valid JSON primitive or array.");
    }

    private static boolean isKeyArray(JsonElement key) {
        return key.isJsonArray();
    }


    public RequestType getType() {
        return this.type;
    }

    public String[] getKey() {
        return this.key;
    }

    public JsonElement getValue() {
        return this.value;
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Request.class, new RequestSerializer()).create();
        return gson.toJson(this, Request.class);
    }

    public boolean isParseKeyAsArray() {
        return parseKeyAsArray;
    }
}
