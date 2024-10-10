package com.chessserver.server;

import com.chessserver.network.ClientMessage;
import com.chessserver.utils.Logger;
import com.chessserver.exceptions.InvalidPseudoException;
import com.chessserver.exceptions.GameFullException;
import com.chessserver.server.PlayerManager;
import com.chessserver.server.GameRegistry;
import com.chessserver.network.Protocol;
import com.chessgame.model.ChessGame;

import java.util.List;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final PlayerManager playerManager;
    private final GameRegistry gameRegistry;
    private String playerPseudo;
    private BufferedReader input;
    private PrintWriter output;
    private GameSession gameSession;

    public ClientHandler(Socket clientSocket, PlayerManager playerManager, GameRegistry gameRegistry) {
        this.clientSocket = clientSocket;
        this.playerManager = playerManager;
        this.gameRegistry = gameRegistry;
    }

    @Override
    public void run() {
        try {
            // Initialise les flux d'entrée et de sortie
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Boucle de communication avec le client
            String message;
            while ((message = input.readLine()) != null) {
                handleMessage(message);
            }

        } catch (IOException e) {
            Logger.error("Erreur de communication avec le client : " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Gère les différents messages reçus du client.
     * Le message peut être de plusieurs types : création de partie, rejoindre une partie, etc.
     */
    private void handleMessage(String message) {
        try {
            Logger.info("Traitement du message : " + message);
            ClientMessage clientMessage = new ClientMessage(message);

            switch (clientMessage.getType()) {
                case Protocol.CONNECT:
                    playerConnection(clientMessage.getPayload());
                    break;
                case Protocol.CREATE_GAME:
                    createGame(clientMessage.getPayload());
                    break;
                case Protocol.JOIN_GAME:
                    joinGame(clientMessage.getPayload());
                    break;
                case Protocol.LIST_GAMES:
                    Logger.info("Liste des parties demandée par " + playerPseudo);
                    listGames();
                    break;
                case Protocol.GET_GAME:
                    output.println(gameRegistry.getGame(clientMessage.getPayload()));
                    break;
                case Protocol.DISCONNECT:
                    closeConnection();
                    break;
                case Protocol.MOVE_PIECE:
                    //TODO: Implémenter la logique pour jouer un coup
                    movePiece(clientMessage.getPayload());
                    break;
                default:
                    output.println("Commande inconnue : " + clientMessage.getType());
            }
        } catch (Exception e) {
            output.println("Erreur lors du traitement du message : " + e.getMessage());
        }
    }

    private void playerConnection(String pseudo) {
        System.out.println("playerConnection");
        System.out.println(pseudo);

        playerPseudo = pseudo;
        // Valide le pseudo avec PlayerManager
        try {
            playerManager.addPlayer(playerPseudo, this);
            output.println("Connexion réussie ! Bienvenue " + playerPseudo);
            Logger.info("Le joueur " + playerPseudo + " s'est connecté.");
        } catch (InvalidPseudoException e) {
            output.println("Erreur : " + e.getMessage());
            closeConnection();
            return;
        }
        output.println(Protocol.OK);
        // Logique de connexion avec pseudo (validation du pseudo, etc.)
    }

    /**
     * Crée une nouvelle partie et notifie le client.
     */
    private void createGame(String gameName) {
        this.gameSession = gameRegistry.createGame(playerPseudo, gameName);
        output.println(Protocol.OK);
        output.println(gameSession.getId());
        output.println("Nouvelle partie créée par " + playerPseudo);
        Logger.info(playerPseudo + " a créé une nouvelle partie.");
    }

    /**
     * Permet au joueur de recupérer la liste des parties disponibles.
     */
    private void listGames() throws IOException, ClassNotFoundException {
        System.out.println("listGames");
        ChessGame game = new ChessGame();


        System.out.println(game);

        List<GameSession> games = gameRegistry.listAvailableGames();

        // Réutilisez le ObjectOutputStream, ou si c'est la première fois que vous l'utilisez, créez-le
        ObjectOutputStream out;
        if (clientSocket.getOutputStream() instanceof ObjectOutputStream) {
            out = (ObjectOutputStream) clientSocket.getOutputStream();
        } else {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        }
        out.writeObject(games);
        out.flush();
    }

    /**
     * Permet au joueur de rejoindre une partie existante.
     */
    private void joinGame(String gameId) {
        try {
            this.gameSession = gameRegistry.joinGame(gameId, playerPseudo);
            output.println(Protocol.OK);
            ObjectOutputStream out;
            if (clientSocket.getOutputStream() instanceof ObjectOutputStream) {
                out = (ObjectOutputStream) clientSocket.getOutputStream();
            } else {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
            }
            out.writeObject(gameSession);

            output.println("Vous avez rejoint la partie " + gameId);
            Logger.info(playerPseudo + " a rejoint la partie " + gameId);
        } catch (Exception e) {
            output.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Ferme la connexion avec le client et le retire du PlayerManager.
     */
    private void closeConnection() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                Logger.info("Connexion fermée pour " + playerPseudo);
            }

            // Retirer le joueur du PlayerManager
            if (playerPseudo != null) {
                playerManager.removePlayer(playerPseudo);
                Logger.info("Le joueur " + playerPseudo + " a été déconnecté.");
            }
        } catch (IOException e) {
            Logger.error("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }


    /**
     * Joue un coup sur le plateau de jeu.
     * @param move Le coup à jouer.
     *
     */
    public void movePiece(String move) {
        //move  = "sourceX;sourceY;destX;destY"
        String[] parts = move.split(";");
        if (parts.length < 4) {
            output.println(Protocol.ERROR_INVALID_MOVE);
            return;
        }
        int[] coordinates = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                coordinates[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                output.println(Protocol.ERROR_INVALID_MOVE);
                return;
            }
        }


    }





}
