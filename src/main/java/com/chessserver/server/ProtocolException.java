package com.chessserver.server;

/**
 * Requête client refusée : porte un code d'erreur du protocole et un message lisible.
 */
public class ProtocolException extends Exception {

    private final String code;

    public ProtocolException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
