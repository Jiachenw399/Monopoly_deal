package network;

public class NetworkMessage {
    private static final String SEPARATOR = "|";

    private final String type;
    private final String body;

    // Creates a NetworkMessage instance.
    public NetworkMessage(String type, String body) {
        this.type = type;
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    // Runs encode.
    public String encode() {
        return type + SEPARATOR + body;
    }

    // Runs decode.
    public static NetworkMessage decode(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return new NetworkMessage("EMPTY", "");
        }

        String[] parts = rawMessage.split("\\|", 2);

        if (parts.length == 1) {
            return new NetworkMessage(parts[0], "");
        }

        return new NetworkMessage(parts[0], parts[1]);
    }
}
