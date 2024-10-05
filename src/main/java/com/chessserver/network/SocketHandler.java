package com.chessserver.network;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SocketHandler.class.getName());

    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean isConnected;

    public SocketHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.isConnected = true;
    }

    @Override
    public void run() {
        try {
            initializeStreams();
            handleClientCommunication();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur de communication avec le client", e);
        } finally {
            closeConnection();
        }
    }

    /**
     * Initialise les flux d'entrée et de sortie du socket.
     *
     * @throws IOException Si une erreur se produit lors de l'initialisation des flux.
     */
    private void initializeStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        output = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    /**
     * Gère la communication avec le client via des messages.
     */
    private void handleClientCommunication() throws IOException {
        String message;
        while (isConnected && (message = input.readLine()) != null) {
            LOGGER.info("Message reçu du client : " + message);
            processMessage(message);
        }
    }

    /**
     * Traite les messages reçus du client et effectue les actions appropriées.
     *
     * @param message Le message reçu du client.
     */
    private void processMessage(String message) {
        // Exemple de traitement des messages en fonction du protocole
        if (message.startsWith(Protocol.CONNECT)) {
            String[] tokens = message.split(";");
            String pseudo = tokens[1];
            handleConnect(pseudo);
        } else if (message.startsWith(Protocol.CREATE_GAME)) {
            handleCreateGame();
        } else if (message.startsWith(Protocol.JOIN_GAME)) {
            String[] tokens = message.split(";");
            String gameId = tokens[1];
            handleJoinGame(gameId);
        } else if (message.startsWith(Protocol.MOVE_PIECE)) {
            String[] tokens = message.split(";");
            int sourceX = Integer.parseInt(tokens[1]);
            int sourceY = Integer.parseInt(tokens[2]);
            int destX = Integer.parseInt(tokens[3]);
            int destY = Integer.parseInt(tokens[4]);
            handleMovePiece(sourceX, sourceY, destX, destY);
        } else if (message.startsWith(Protocol.DISCONNECT)) {
            handleDisconnect();
        }
    }

    /**
     * Gère la connexion d'un joueur en vérifiant le pseudo.
     *
     * @param pseudo Le pseudo du joueur.
     */
    private void handleConnect(String pseudo) {
        // Logique de connexion avec pseudo (validation du pseudo, etc.)
        output.println(Protocol.OK);  // Envoyer une réponse de succès
    }

    /**
     * Gère la création d'une nouvelle partie.
     */
    private void handleCreateGame() {
        // Logique pour créer une nouvelle partie
        output.println(Protocol.GAME_CREATED);  // Envoyer une réponse de création de partie
    }

    /**
     * Gère la demande de rejoindre une partie.
     *
     * @param gameId L'ID de la partie que le client souhaite rejoindre.
     */
    private void handleJoinGame(String gameId) {
        // Logique pour rejoindre une partie
        output.println(Protocol.GAME_JOINED);  // Envoyer une réponse de succès
    }

    /**
     * Gère le déplacement d'une pièce sur l'échiquier.
     *
     * @param sourceX Position x de départ.
     * @param sourceY Position y de départ.
     * @param destX   Position x de destination.
     * @param destY   Position y de destination.
     */
    private void handleMovePiece(int sourceX, int sourceY, int destX, int destY) {
        // Logique pour traiter un mouvement de pièce
        output.println(Protocol.MOVE_ACCEPTED);  // Envoyer une réponse de succès
    }

    /**
     * Gère la déconnexion du client.
     */
    private void handleDisconnect() {
        this.isConnected = false;
        LOGGER.info("Client déconnecté");
    }

    /**
     * Ferme la connexion du client et les flux associés.
     */
    private void closeConnection() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            LOGGER.info("Connexion fermée");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture des ressources", e);
        }
    }

    public PrintWriter getOutput() {
        return output;
    }

}
