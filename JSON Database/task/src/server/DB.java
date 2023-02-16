package server;

import client.Request;
import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Singleton class that stores a key-value database as a JSON
 * and keeps/loads it in a Hashmap during runtime.
 * Writes to disc after every set/delete operation.
 */

public class DB {

    private final String dbFolder;
    private final String dbFile;
    private final Path dbPath;
    private static DB dbInstance = null;
    private static JsonObject db;

    public static DB getDb() {
        if (dbInstance == null)
            throw new RuntimeException("You need to initialise DB first with 'startNewDb()'.");
        return dbInstance;
    }

    public static void startNewDb(String dbFolder, String dbFile) throws IOException {
        dbInstance = new DB(dbFolder, dbFile);
        dbInstance.setUpDb();
    }

    private void writeDbToFile() {
        try (FileWriter writer = new FileWriter(this.dbPath.toFile())) {
            writer.write(dbInstance.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DB(String dbFolder, String dbFile) {
        this.dbFolder = dbFolder;
        this.dbFile = dbFile;
        this.dbPath = Paths.get(this.dbFolder, this.dbFile);
    }

    private void setUpDb() throws IOException {
        Files.createDirectories(Paths.get(this.dbFolder)); // Creates if not exists per default
        if (Files.exists(this.dbPath)) {
            this.readExistingJson();
        } else {
            Files.createFile(this.dbPath);
        }
    }

    private void readExistingJson() throws FileNotFoundException {
        JsonParser parser = new JsonParser();
        if (this.dbPath.toFile().length() == 0) {
            this.db = new JsonObject();
            return;
        }

        this.db = parser.parse(new FileReader(this.dbPath.toFile())).getAsJsonObject();
        if (this.db == null)
            this.db = new JsonObject();
    }

    // Recursive method to set a value in a nested JsonObject
    public boolean setValue(String[] key, JsonObject dbSubset, JsonElement value) {
        if (key.length == 1) {
            dbSubset.add(key[0], value);
        } else {
            if (!dbSubset.has(key[0])) {
                dbSubset.add(key[0], new JsonObject());
            }
            setValue(Arrays.copyOfRange(key, 1, key.length),
                    dbSubset.getAsJsonObject(key[0]), value);
        }

        writeDbToFile();
        return true;
    }

    // Recursive method to get a value from a nested JsonObject
    public JsonElement getValue(String[] key, JsonObject jsonObject) {
        if (key.length == 1) {
            if (!jsonObject.has(key[0]) || jsonObject.get(key[0]).isJsonNull())
                return null;
            else
                return jsonObject.get(key[0]);
        } else if (!jsonObject.has(key[0])) {
            return null;
        } else {
            return getValue(Arrays.copyOfRange(key, 1, key.length),
                    jsonObject.getAsJsonObject(key[0])); // Recurse until key.length == 1
        }
    }

    public boolean deleteValue(String[] keys) {
        int i = 0;
        JsonElement current = this.db;

        while (i < keys.length - 1) {
            if (!current.isJsonObject()) {
                return false; // key does not exist
            }
            current = current.getAsJsonObject().get(keys[i]);
            i++;
        }

        // Remove the nested element
        if (current.isJsonObject() && current.getAsJsonObject().has(keys[i])) {
                current.getAsJsonObject().remove(keys[i]);
                writeDbToFile();
                return true;
        } else
            return false; // key does not exist
    }

    private String toJson() {
        GsonBuilder builder = new GsonBuilder();
        // builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this.db);
    }

    public JsonObject getDbContent() {
        return this.db;
    }
}
