package com.chessserver.server;

import com.chessserver.network.ClientMessage;
import com.chessserver.utils.Logger;
import com.chessserver.exceptions.InvalidPseudoException;
import com.chessserver.exceptions.GameFullException;
import com.chessserver.server.PlayerManager;
import com.chessserver.server.GameRegistry;
import com.chessserver.network.Protocol;

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

            // Demande au client de fournir un pseudo
            output.println("Veuillez entrer votre pseudo : ");
            playerPseudo = input.readLine();

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
            ClientMessage clientMessage = parseMessage(message);

            switch (clientMessage.getType()) {
                case Protocol.CREATE_GAME:
                    createGame();
                    break;
                case Protocol.JOIN_GAME:
                    joinGame(clientMessage.getPayload());
                    break;
                case Protocol.LIST_GAMES:
                    output.println(gameRegistry.listAvailableGames());
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

    /**
     * Crée une nouvelle partie et notifie le client.
     */
    private void createGame() {
        this.gameSession = gameRegistry.createGame(playerPseudo);
        output.println("Nouvelle partie créée par " + playerPseudo);
        Logger.info(playerPseudo + " a créé une nouvelle partie.");
    }

    /**
     * Permet au joueur de rejoindre une partie existante.
     */
    private void joinGame(String gameId) {
        try {
            this.gameSession = gameRegistry.joinGame(gameId, playerPseudo);
            output.println("Vous avez rejoint la partie " + gameId);
            Logger.info(playerPseudo + " a rejoint la partie " + gameId);
            //GameFullException
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
     * Parse le message reçu du client en un objet `ClientMessage`.
     */
    private ClientMessage parseMessage(String message) {
        // Ici, on peut utiliser un parseur JSON, mais pour simplifier on fait un parsing manuel.
        String[] parts = message.split(":");
        ClientMessage clientMessage = new ClientMessage();
        clientMessage.setType(parts[0]);
        if (parts.length > 1) {
            clientMessage.setPayload(parts[1]);
        }
        return clientMessage;
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
