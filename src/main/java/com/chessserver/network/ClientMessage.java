package com.chessserver.network;

public class ClientMessage {
    private String type; // "connect", "createGame", "joinGame"
    private String payload; // Peut être le pseudo ou l'ID de la partie

    public ClientMessage(String message) {
        String[] tokens = message.split(";");
        this.type = tokens[0];
        this.payload = tokens.length > 1 ? tokens[1] : null;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public void setType(String part) {
        this.type = type;
    }

    public void setPayload(String part) {
        this.payload = payload;
    }

    // Getters et setters
}

