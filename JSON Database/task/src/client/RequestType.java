package client;

public enum RequestType {
    GET("get"),
    SET("set"),
    DELETE("delete"),
    EXIT("exit");

    private final String type;

    RequestType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static RequestType fromString(String type) {
        for (RequestType requestType : RequestType.values()) {
            if (requestType.type.equalsIgnoreCase(type)) {
                return requestType;
            }
        }
        return null;
    }
}
