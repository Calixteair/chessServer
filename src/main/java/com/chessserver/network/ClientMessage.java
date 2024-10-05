package com.chessserver.network;

public class ClientMessage {
    private String type; // "connect", "createGame", "joinGame"
    private String payload; // Peut être le pseudo ou l'ID de la partie

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

