package server;

import com.google.gson.*;

import java.lang.reflect.Type;

public class Response implements JsonSerializer<Response> {
    private final String responseCode;
    private final String message;
    private final JsonElement responseValue;

    public Response(String responseCode) {
        this.responseCode = responseCode;
        this.message = null;
        this.responseValue = null;
    }

    public Response(String responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
        this.responseValue = null;
    }

    public Response(String responseCode, JsonElement responseValue) {
        this.responseCode = responseCode;
        this.message = null;
        this.responseValue = responseValue;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }

    // Return "value" if response is OK and "reason" if response is ERROR
    @Override
    public JsonElement serialize(Response response, Type type,
                                 JsonSerializationContext jsonSerializationContext) {

        JsonObject responseJsonObj = new JsonObject();

        // Customize the JsonObject
        responseJsonObj.addProperty("response", response.getResponseCode());
        if (response.getResponseCode().equals("OK") && response.getMessage() != null) {
            responseJsonObj.addProperty("value", response.getMessage());
        } else if (response.getResponseCode().equals("OK") && response.responseValue != null) {
            responseJsonObj.add("value", response.responseValue);
        } else if (response.getResponseCode().equals("ERROR") && response.getMessage() != null) {
            responseJsonObj.addProperty("reason", response.getMessage());
        }

        return responseJsonObj;
    }

    public String toJson() {
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(Response.class, this);
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}
