package com.chessserver.config;

/**
 * Ports d'écoute, surchargeables par variables d'environnement.
 */
public final class ServerConfig {

    private static final int DEFAULT_TCP_PORT = 8090;
    private static final int DEFAULT_WS_PORT = 8091;

    private ServerConfig() {
    }

    /** Port TCP du client JavaFX ({@code CHESS_TCP_PORT}, 8090 par défaut). */
    public static int getTcpPort() {
        return readPort("CHESS_TCP_PORT", DEFAULT_TCP_PORT);
    }

    /** Port WebSocket du client web ({@code CHESS_WS_PORT}, 8091 par défaut). */
    public static int getWebSocketPort() {
        return readPort("CHESS_WS_PORT", DEFAULT_WS_PORT);
    }

    private static int readPort(String variable, int fallback) {
        String value = System.getenv(variable);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
