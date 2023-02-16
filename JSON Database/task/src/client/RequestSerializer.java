package client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class RequestSerializer implements JsonSerializer<Request> {

    @Override
    public JsonElement serialize(Request request, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("type", request.getType().toString().toLowerCase());
        json.add("key", parseKey(request, context));
        json.add("value", request.getValue());
        return json;
    }

    private JsonElement parseKey(Request request, JsonSerializationContext context) {
        if (request.getKey() == null) {
            return null;
        } else if (request.getKey().length == 1) {
            if (request.isParseKeyAsArray()) {
                return context.serialize(request.getKey());
            } else {
                return context.serialize(request.getKey()[0]);
            }
        } else {
            return context.serialize(request.getKey());
        }
    }
}
