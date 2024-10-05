package com.chessserver.config;

public class ServerConfig {

    private static final int DEFAULT_PORT = 8090;

    public static int getPort() {
        // Récupère le port depuis un fichier de configuration ou retourne la valeur par défaut
        return DEFAULT_PORT;
    }
}

