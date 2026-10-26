package com.chessserver.network;

import com.chessgame.model.ChessGame;
import com.chessgame.utils.Move;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Protocole texte partagé par le client JavaFX (TCP) et le client web (WebSocket).
 * Un message = une ligne, champs séparés par ';'. Le serveur fait autorité : il valide chaque
 * coup et pousse l'état complet de la partie aux deux joueurs après chaque changement.
 *
 * Client → serveur :
 *   CONNECT;pseudo · LIST_GAMES · CREATE_GAME;nom · JOIN_GAME;id · MOVE;e2e4 (promotion : e7e8q)
 *   RESIGN · LEAVE_GAME · PING · DISCONNECT
 *
 * Serveur → client :
 *   WELCOME;pseudo
 *   GAMES;id,nom,créateur|id,nom,créateur…      (liste des parties en attente, poussée à chaque changement)
 *   GAME_CREATED;id;nom                          (en attente d'un adversaire)
 *   GAME_START;id;nom;pseudoBlancs;pseudoNoirs;w|b (couleur du destinataire)
 *   STATE;fen;statut;vainqueur(w|b|-);échec(0|1);dernierCoup(uci|-);historiqueSAN;coupsLégaux(uci)
 *         (historique et coups légaux séparés par des espaces)
 *   OPPONENT_LEFT;pseudo · PONG · ERROR;code;message
 */
public final class Protocol {

    public static final String SEP = ";";

    // --- Commandes client ---
    public static final String CONNECT = "CONNECT";
    public static final String LIST_GAMES = "LIST_GAMES";
    public static final String CREATE_GAME = "CREATE_GAME";
    public static final String JOIN_GAME = "JOIN_GAME";
    public static final String MOVE = "MOVE";
    public static final String RESIGN = "RESIGN";
    public static final String LEAVE_GAME = "LEAVE_GAME";
    public static final String PING = "PING";
    public static final String DISCONNECT = "DISCONNECT";

    // --- Messages serveur ---
    public static final String WELCOME = "WELCOME";
    public static final String GAMES = "GAMES";
    public static final String GAME_CREATED = "GAME_CREATED";
    public static final String GAME_START = "GAME_START";
    public static final String STATE = "STATE";
    public static final String OPPONENT_LEFT = "OPPONENT_LEFT";
    public static final String PONG = "PONG";
    public static final String ERROR = "ERROR";

    // --- Codes d'erreur ---
    public static final String ERR_INVALID_PSEUDO = "INVALID_PSEUDO";
    public static final String ERR_PSEUDO_TAKEN = "PSEUDO_TAKEN";
    public static final String ERR_NOT_CONNECTED = "NOT_CONNECTED";
    public static final String ERR_ALREADY_CONNECTED = "ALREADY_CONNECTED";
    public static final String ERR_INVALID_GAME_NAME = "INVALID_GAME_NAME";
    public static final String ERR_GAME_NOT_FOUND = "GAME_NOT_FOUND";
    public static final String ERR_GAME_FULL = "GAME_FULL";
    public static final String ERR_ALREADY_IN_GAME = "ALREADY_IN_GAME";
    public static final String ERR_NOT_IN_GAME = "NOT_IN_GAME";
    public static final String ERR_NOT_YOUR_TURN = "NOT_YOUR_TURN";
    public static final String ERR_ILLEGAL_MOVE = "ILLEGAL_MOVE";
    public static final String ERR_UNKNOWN_COMMAND = "UNKNOWN_COMMAND";

    /** Longueur maximale d'une ligne reçue ; au-delà le message est ignoré. */
    public static final int MAX_LINE_LENGTH = 512;

    private Protocol() {
    }

    public static String message(String type, Object... fields) {
        StringBuilder sb = new StringBuilder(type);
        for (Object f : fields) {
            sb.append(SEP).append(f);
        }
        return sb.toString();
    }

    public static String error(String code, String text) {
        return message(ERROR, code, text);
    }

    public static String state(ChessGame game) {
        Boolean winner = game.getWinnerIsWhite();
        Move last = game.getLastMove();
        String legal = game.getLegalMoves().stream().map(Move::toUci).collect(Collectors.joining(" "));
        return message(STATE,
                game.getFen(),
                game.getStatus().name(),
                winner == null ? "-" : (winner ? "w" : "b"),
                game.isCheck() ? 1 : 0,
                last == null ? "-" : last.toUci(),
                String.join(" ", game.getSanHistory()),
                legal);
    }

    /** Entrée de la liste des parties : id,nom,créateur (les noms ne contiennent ni ',' ni '|'). */
    public static String games(List<String> entries) {
        return message(GAMES, String.join("|", entries));
    }
}
