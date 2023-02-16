package client;

import com.google.gson.*;

import java.lang.reflect.Type;

public class RequestDeserializer implements JsonDeserializer<Request> {
    @Override
    public Request deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String requestType = jsonObject.get("type").getAsString();
        JsonElement value = jsonObject.get("value");

        String[] key;
        JsonElement keyElement = jsonObject.get("key");
        if (keyElement == null) {
            key = null;
        } else if (keyElement.isJsonPrimitive()) {
            key = new String[]{ keyElement.getAsString() };
        } else {
            key = context.deserialize(keyElement, String[].class);
        }

        return new Request(RequestType.fromString(requestType), key, value, false);
    }
}
