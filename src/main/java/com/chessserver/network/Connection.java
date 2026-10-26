package com.chessserver.network;

/**
 * Canal vers un client, indépendant du transport (socket TCP ou WebSocket).
 * Les implémentations doivent permettre des envois concurrents depuis plusieurs threads.
 */
public interface Connection {

    /** Envoie une ligne de protocole. Ne lève pas d'exception si le client est déjà parti. */
    void send(String line);

    void close();

    /** Description courte pour les logs (adresse distante, transport). */
    String describe();
}
