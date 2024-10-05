package com.chessserver.network;

/**
 * Cette classe contient toutes les commandes et messages échangés entre le client et le serveur.
 * Elle permet d'unifier les types de requêtes et de réponses dans le jeu d'échecs multijoueur.
 */
public class Protocol {

    // --- Commandes envoyées par le client ---

    public static final String CONNECT = "CONNECT";          // Connexion au serveur avec un pseudo
    public static final String CREATE_GAME = "CREATE_GAME";  // Création d'une nouvelle partie
    public static final String JOIN_GAME = "JOIN_GAME";      // Rejoindre une partie existante
    public static final String MOVE_PIECE = "MOVE_PIECE";    // Déplacer une pièce
    public static final String LIST_GAMES = "LIST_GAMES";    // Demander la liste des parties disponibles
    public static final String LEAVE_GAME = "LEAVE_GAME";    // Quitter une partie
    public static final String DISCONNECT = "DISCONNECT";    // Déconnexion du serveur

    // --- Réponses du serveur ---

    public static final String OK = "OK";                     // Réponse pour indiquer que la commande a réussi
    public static final String ERROR = "ERROR";               // Réponse pour indiquer une erreur
    public static final String GAME_CREATED = "GAME_CREATED"; // Réponse indiquant qu'une partie a été créée
    public static final String GAME_JOINED = "GAME_JOINED";   // Réponse indiquant qu'une partie a été rejointe
    public static final String MOVE_ACCEPTED = "MOVE_ACCEPTED"; // Réponse indiquant qu'un déplacement a été accepté
    public static final String MOVE_REJECTED = "MOVE_REJECTED"; // Réponse indiquant qu'un déplacement est invalide
    public static final String CHECK = "CHECK";               // Réponse indiquant que le roi est en échec
    public static final String CHECKMATE = "CHECKMATE";       // Réponse indiquant que la partie est en échec et mat
    public static final String DRAW = "DRAW";                 // Réponse indiquant une partie nulle
    public static final String GAME_FULL = "GAME_FULL";       // Réponse indiquant que la partie est pleine
    public static final String LIST_OF_GAMES = "LIST_OF_GAMES"; // Envoi de la liste des parties disponibles

    // --- Messages pour la gestion d'erreurs ---

    public static final String ERROR_INVALID_PSEUDO = "ERROR_INVALID_PSEUDO"; // Erreur si le pseudo est invalide
    public static final String ERROR_GAME_NOT_FOUND = "ERROR_GAME_NOT_FOUND"; // Erreur si la partie n'existe pas
    public static final String ERROR_GAME_FULL = "ERROR_GAME_FULL";           // Erreur si la partie est pleine
    public static final String ERROR_INVALID_MOVE = "ERROR_INVALID_MOVE";     // Erreur si le mouvement est invalide
    public static final String ERROR_NOT_YOUR_TURN = "ERROR_NOT_YOUR_TURN";   // Erreur si ce n'est pas le tour du joueur

    // --- Messages spécifiques aux échecs ---

    public static final String PROMOTION = "PROMOTION";      // Promotion d'un pion
    public static final String CASTLING = "CASTLING";        // Roque
    public static final String EN_PASSANT = "EN_PASSANT";    // Prise en passant

    /**
     * Exemple de format pour les messages de déplacement :
     * MOVE_PIECE;sourceX;sourceY;destX;destY
     * Exemple : MOVE_PIECE;4;1;4;3 signifie déplacer une pièce de la case (4,1) à (4,3).
     */
    public static String formatMove(int sourceX, int sourceY, int destX, int destY) {
        return String.format("%s;%d;%d;%d;%d", MOVE_PIECE, sourceX, sourceY, destX, destY);
    }

    /**
     * Exemple de format pour une requête de connexion :
     * CONNECT;pseudo
     * Exemple : CONNECT;JohnDoe
     */
    public static String formatConnect(String pseudo) {
        return String.format("%s;%s", CONNECT, pseudo);
    }

    /**
     * Formater la création d'une partie.
     * Exemple : CREATE_GAME
     */
    public static String formatCreateGame() {
        return CREATE_GAME;
    }

    /**
     * Formater une demande pour rejoindre une partie.
     * Exemple : JOIN_GAME;gameId
     */
    public static String formatJoinGame(String gameId) {
        return String.format("%s;%s", JOIN_GAME, gameId);
    }

    /**
     * Formater la liste des parties disponibles.
     * Exemple : LIST_GAMES
     */
    public static String formatListGames() {
        return LIST_GAMES;
    }

    /**
     * Formater une demande pour quitter une partie.
     * Exemple : LEAVE_GAME
     */
    public static String formatLeaveGame() {
        return LEAVE_GAME;
    }
}
