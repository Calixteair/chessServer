package com.chessserver.server;

import com.chessgame.model.ChessGame;
import com.chessgame.utils.Move;
import com.chessserver.network.Protocol;

/**
 * Une partie entre deux sessions. Le créateur joue les blancs.
 * Toutes les règles sont arbitrées par {@link ChessGame} ; la salle vérifie seulement
 * que c'est bien au joueur de jouer, puis diffuse le nouvel état aux deux camps.
 */
public class GameRoom {

    private final String id;
    private final String name;
    private ClientSession white;
    private ClientSession black;
    private ChessGame game;

    public GameRoom(String id, String name, ClientSession creator) {
        this.id = id;
        this.name = name;
        this.white = creator;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Entrée « id,nom,créateur » si la partie attend un adversaire, sinon null. */
    synchronized String lobbyEntry() {
        return isWaiting() ? String.join(",", id, name, white.getPseudo()) : null;
    }

    private boolean isWaiting() {
        return game == null && white != null && black == null;
    }

    synchronized boolean tryJoin(ClientSession player) {
        if (!isWaiting() || player == white) {
            return false;
        }
        black = player;
        game = new ChessGame();
        return true;
    }

    synchronized void announceStart() {
        white.send(startMessage("w"));
        black.send(startMessage("b"));
        broadcastState();
    }

    private String startMessage(String color) {
        return Protocol.message(Protocol.GAME_START, id, name, white.getPseudo(), black.getPseudo(), color);
    }

    synchronized void move(ClientSession player, String uci) throws ProtocolException {
        ChessGame current = requireStarted();
        if (current.isGameOver()) {
            throw new ProtocolException(Protocol.ERR_ILLEGAL_MOVE, "La partie est terminée");
        }
        if (current.isWhiteToMove() != (player == white)) {
            throw new ProtocolException(Protocol.ERR_NOT_YOUR_TURN, "Ce n'est pas votre tour");
        }
        Move move;
        try {
            move = Move.fromUci(uci);
        } catch (IllegalArgumentException e) {
            throw new ProtocolException(Protocol.ERR_ILLEGAL_MOVE, "Coup mal formé : " + uci);
        }
        if (!current.play(move)) {
            throw new ProtocolException(Protocol.ERR_ILLEGAL_MOVE, "Coup illégal : " + uci);
        }
        broadcastState();
    }

    synchronized void resign(ClientSession player) throws ProtocolException {
        requireStarted().resign(player == white);
        broadcastState();
    }

    /** Un joueur quitte la salle ; si la partie était en cours, il la perd par abandon. */
    synchronized void leave(ClientSession player) {
        boolean wasWhite = player == white;
        if (wasWhite) {
            white = null;
        } else if (player == black) {
            black = null;
        } else {
            return;
        }
        if (game == null) {
            return;
        }
        game.abandon(wasWhite);
        broadcastState();
        broadcast(Protocol.message(Protocol.OPPONENT_LEFT, player.getPseudo()));
    }

    synchronized boolean isEmpty() {
        return white == null && black == null;
    }

    private ChessGame requireStarted() throws ProtocolException {
        if (game == null) {
            throw new ProtocolException(Protocol.ERR_NOT_IN_GAME, "La partie n'a pas encore commencé");
        }
        return game;
    }

    private void broadcastState() {
        broadcast(Protocol.state(game));
    }

    /** Envoie aux joueurs encore assis. */
    private void broadcast(String line) {
        if (white != null) white.send(line);
        if (black != null) black.send(line);
    }
}
