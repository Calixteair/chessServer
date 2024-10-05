package com.chessserver.network;


import com.chessserver.network.Protocol;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    private final SocketHandler socketHandler;

    public MessageHandler(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    /**
     * Traite le message reçu du client.
     *
     * @param message Le message reçu.
     */
    public void handleMessage(String message) {
        LOGGER.info("Traitement du message : " + message);

        if (message.startsWith(Protocol.CONNECT)) {
            handleConnect(message);
        } else if (message.startsWith(Protocol.CREATE_GAME)) {
            handleCreateGame();
        } else if (message.startsWith(Protocol.JOIN_GAME)) {
            handleJoinGame(message);
        } else if (message.startsWith(Protocol.MOVE_PIECE)) {
            handleMovePiece(message);
        } else if (message.startsWith(Protocol.DISCONNECT)) {
            handleDisconnect();
        } else {
            socketHandler.getOutput().println(Protocol.ERROR); // Réponse pour message non reconnu
        }
    }

    private void handleConnect(String message) {
        String[] tokens = message.split(";");
        if (tokens.length < 2) {
            socketHandler.getOutput().println(Protocol.ERROR_INVALID_PSEUDO);
            return;
        }

        String pseudo = tokens[1];
        // Logique de validation du pseudo ici
        socketHandler.getOutput().println(Protocol.OK);  // Envoi de la réponse de succès
    }

    private void handleCreateGame() {
        // Logique pour créer une nouvelle partie
        socketHandler.getOutput().println(Protocol.GAME_CREATED);  // Envoi de la réponse de création de partie
    }

    private void handleJoinGame(String message) {
        String[] tokens = message.split(";");
        if (tokens.length < 2) {
            socketHandler.getOutput().println(Protocol.ERROR_GAME_NOT_FOUND);
            return;
        }

        String gameId = tokens[1];
        // Logique pour rejoindre une partie ici
        socketHandler.getOutput().println(Protocol.GAME_JOINED);  // Envoi de la réponse de succès
    }

    private void handleMovePiece(String message) {
        String[] tokens = message.split(";");
        if (tokens.length < 5) {
            socketHandler.getOutput().println(Protocol.ERROR_INVALID_MOVE);
            return;
        }

        try {
            int sourceX = Integer.parseInt(tokens[1]);
            int sourceY = Integer.parseInt(tokens[2]);
            int destX = Integer.parseInt(tokens[3]);
            int destY = Integer.parseInt(tokens[4]);
            // Logique pour valider et exécuter le mouvement ici
            socketHandler.getOutput().println(Protocol.MOVE_ACCEPTED);  // Envoi de la réponse de succès
        } catch (NumberFormatException e) {
            socketHandler.getOutput().println(Protocol.ERROR_INVALID_MOVE);
        }
    }

    private void handleDisconnect() {
        // Logique pour gérer la déconnexion
        socketHandler.getOutput().println(Protocol.OK); // Envoi d'une réponse de déconnexion
    }
}