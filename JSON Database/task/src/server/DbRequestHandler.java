package server;

import client.Request;
import com.google.gson.JsonElement;

public class DbRequestHandler {

    public static Response handleAnyRequest(Request request, DB db) {
        switch (request.getType()) {
            case GET :
                return handleGetRequest(request, db);
            case DELETE:
                return handleDeleteRequest(request, db);
            case SET:
                return handleSetRequest(request, db);
            case EXIT:
                return new Response("OK");
            default:
                return new Response("ERROR");
        }
    }

    private static Response handleGetRequest(Request request, DB db) {
        JsonElement responseValue = db.getValue(request.getKey(), db.getDbContent());
        if (responseValue == null)
            return new Response("ERROR", "No such key");
        else
            return new Response("OK", responseValue);

    }

    private static Response handleSetRequest(Request request, DB db) {
        if (db.setValue(request.getKey(), db.getDbContent(), request.getValue()))
            return new Response("OK");
        else
            return new Response("ERROR");
    }

    private static Response handleDeleteRequest(Request request, DB db) {
        if (db.deleteValue(request.getKey()))
            return new Response("OK");
        else
            return new Response("ERROR", "No such key");
    }

}
