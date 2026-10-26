package com.chessserver.server;

import com.chessserver.network.Connection;
import com.chessserver.network.Protocol;
import com.chessserver.utils.Logger;

/**
 * État d'un client connecté (pseudo, partie en cours) et traitement de ses commandes.
 * Indépendant du transport : TCP et WebSocket créent chacun une session par client.
 */
public class ClientSession {

    private final Connection connection;
    private final Lobby lobby;
    private volatile String pseudo;
    private volatile GameRoom room;

    public ClientSession(Connection connection, Lobby lobby) {
        this.connection = connection;
        this.lobby = lobby;
    }

    /** Traite une ligne reçue. Les commandes d'un même client sont traitées une à une. */
    public synchronized void onMessage(String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        if (line.length() > Protocol.MAX_LINE_LENGTH) {
            send(Protocol.error(Protocol.ERR_UNKNOWN_COMMAND, "Message trop long"));
            return;
        }
        String[] parts = line.trim().split(Protocol.SEP, 2);
        String argument = parts.length > 1 ? parts[1].trim() : "";
        try {
            dispatch(parts[0], argument);
        } catch (ProtocolException e) {
            send(Protocol.error(e.getCode(), e.getMessage()));
        }
    }

    private void dispatch(String command, String argument) throws ProtocolException {
        switch (command) {
            case Protocol.PING:
                send(Protocol.PONG);
                return;
            case Protocol.CONNECT:
                connect(argument);
                return;
            case Protocol.DISCONNECT:
                connection.close();
                return;
            default:
                break;
        }
        if (pseudo == null) {
            throw new ProtocolException(Protocol.ERR_NOT_CONNECTED, "Connectez-vous d'abord avec un pseudo");
        }
        switch (command) {
            case Protocol.LIST_GAMES:
                send(lobby.gamesMessage());
                break;
            case Protocol.CREATE_GAME:
                requireNoRoom();
                GameRoom created = lobby.createGame(this, argument);
                send(Protocol.message(Protocol.GAME_CREATED, created.getId(), created.getName()));
                break;
            case Protocol.JOIN_GAME:
                requireNoRoom();
                lobby.joinGame(this, argument);
                break;
            case Protocol.MOVE:
                requireRoom().move(this, argument);
                break;
            case Protocol.RESIGN:
                requireRoom().resign(this);
                break;
            case Protocol.LEAVE_GAME:
                requireRoom();
                leaveRoom(); // le lobby repousse la liste des parties
                break;
            default:
                throw new ProtocolException(Protocol.ERR_UNKNOWN_COMMAND, "Commande inconnue : " + command);
        }
    }

    private void connect(String requested) throws ProtocolException {
        if (pseudo != null) {
            throw new ProtocolException(Protocol.ERR_ALREADY_CONNECTED, "Déjà connecté en tant que " + pseudo);
        }
        lobby.register(this, requested);
        pseudo = requested;
        Logger.info(pseudo + " connecté (" + connection.describe() + ")");
        send(Protocol.message(Protocol.WELCOME, pseudo));
        send(lobby.gamesMessage());
    }

    private void requireNoRoom() throws ProtocolException {
        if (room != null) {
            throw new ProtocolException(Protocol.ERR_ALREADY_IN_GAME, "Quittez d'abord votre partie en cours");
        }
    }

    private GameRoom requireRoom() throws ProtocolException {
        GameRoom current = room;
        if (current == null) {
            throw new ProtocolException(Protocol.ERR_NOT_IN_GAME, "Vous n'êtes dans aucune partie");
        }
        return current;
    }

    /** Appelé par le lobby dès que la session est assise dans une salle, avant toute diffusion. */
    void enterRoom(GameRoom joined) {
        room = joined;
    }

    private void leaveRoom() {
        GameRoom left = room;
        room = null;
        if (left != null) {
            lobby.leaveGame(this, left);
        }
    }

    /** Appelé par le transport quand la connexion est fermée, quelle qu'en soit la cause. */
    public synchronized void onClose() {
        leaveRoom();
        if (pseudo != null) {
            lobby.unregister(this);
            Logger.info(pseudo + " déconnecté");
        }
    }

    public void send(String line) {
        connection.send(line);
    }

    public String getPseudo() {
        return pseudo;
    }

    public boolean isInRoom() {
        return room != null;
    }
}
