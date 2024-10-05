package com.chessserver.server;

import com.chessserver.utils.Logger;
import com.chessserver.config.ServerConfig;
import com.chessserver.network.SocketHandler;
import com.chessserver.server.PlayerManager;
import com.chessserver.server.GameRegistry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChessServer {

    private final int port;
    private final PlayerManager playerManager;
    private final GameRegistry gameRegistry;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;

    public ChessServer(int port) {
        this.port = port;
        this.playerManager = new PlayerManager();
        this.gameRegistry = new GameRegistry();
        this.threadPool = Executors.newCachedThreadPool();  // Utilisation d'un pool de threads pour gérer les clients
    }

    /**
     * Démarre le serveur et accepte les connexions clients.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            Logger.info("Serveur démarré sur le port " + port);

            // Boucle infinie pour accepter les connexions des clients
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                Logger.info("Nouvelle connexion de " + clientSocket.getInetAddress());

                // Crée un thread pour gérer le client via ClientHandler
                ClientHandler clientHandler = new ClientHandler(clientSocket, playerManager, gameRegistry);
                threadPool.execute(clientHandler);  // Utilisation du thread pool pour gérer le client
            }
        } catch (IOException e) {
            Logger.error("Erreur lors du démarrage du serveur : " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    /**
     * Arrête le serveur proprement en fermant le ServerSocket et le pool de threads.
     */
    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Logger.info("Serveur arrêté.");
            }
            threadPool.shutdown();
        } catch (IOException e) {
            Logger.error("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Récupère la configuration du serveur
        int port = ServerConfig.getPort();
        ChessServer server = new ChessServer(port);

        // Démarre le serveur
        server.start();
    }
}
