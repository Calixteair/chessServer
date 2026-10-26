package com.chessserver.server;

import com.chessserver.config.ServerConfig;
import com.chessserver.network.TcpConnection;
import com.chessserver.network.WebSocketGateway;
import com.chessserver.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serveur d'échecs : un port TCP (client JavaFX) et un port WebSocket (client web),
 * branchés sur le même lobby. Un joueur FX peut donc affronter un joueur web.
 */
public class ChessServer {

    private final Lobby lobby = new Lobby();
    private final ExecutorService tcpThreads = Executors.newCachedThreadPool();
    private final int tcpPort;
    private final WebSocketGateway webSocket;
    private ServerSocket serverSocket;

    public ChessServer(int tcpPort, int wsPort) {
        this.tcpPort = tcpPort;
        this.webSocket = new WebSocketGateway(wsPort, lobby);
    }

    public void start() {
        webSocket.start();
        try (ServerSocket socket = new ServerSocket(tcpPort)) {
            serverSocket = socket;
            Logger.info("TCP à l'écoute sur le port " + tcpPort);
            while (!socket.isClosed()) {
                acceptOne(socket);
            }
        } catch (IOException e) {
            Logger.error("Arrêt du serveur TCP : " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void acceptOne(ServerSocket socket) throws IOException {
        Socket client = socket.accept();
        try {
            tcpThreads.execute(new TcpConnection(client, lobby));
        } catch (IOException e) {
            Logger.warn("Connexion TCP refusée : " + e.getMessage());
            client.close();
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            webSocket.stop(1000);
        } catch (IOException | InterruptedException e) {
            Logger.error("Erreur à l'arrêt : " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        tcpThreads.shutdownNow();
    }

    public static void main(String[] args) {
        ChessServer server = new ChessServer(ServerConfig.getTcpPort(), ServerConfig.getWebSocketPort());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.start();
    }
}
