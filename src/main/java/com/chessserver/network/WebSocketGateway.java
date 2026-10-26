package com.chessserver.network;

import com.chessserver.server.ClientSession;
import com.chessserver.server.Lobby;
import com.chessserver.utils.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * Point d'entrée WebSocket pour le client web : même protocole texte que TCP,
 * un message WebSocket = une ligne de protocole.
 */
public class WebSocketGateway extends WebSocketServer {

    private final Lobby lobby;

    public WebSocketGateway(int port, Lobby lobby) {
        super(new InetSocketAddress(port));
        this.lobby = lobby;
        setReuseAddr(true);
        setConnectionLostTimeout(30);
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        socket.setAttachment(new ClientSession(new WsConnection(socket), lobby));
    }

    @Override
    public void onMessage(WebSocket socket, String message) {
        ClientSession session = socket.getAttachment();
        if (session != null) {
            session.onMessage(message);
        }
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        ClientSession session = socket.getAttachment();
        if (session != null) {
            session.onClose();
        }
    }

    @Override
    public void onError(WebSocket socket, Exception e) {
        Logger.error("WebSocket " + (socket == null ? "serveur" : socket.getRemoteSocketAddress()) + " : " + e.getMessage());
    }

    @Override
    public void onStart() {
        Logger.info("WebSocket à l'écoute sur le port " + getPort());
    }

    private static final class WsConnection implements Connection {
        private final WebSocket socket;

        WsConnection(WebSocket socket) {
            this.socket = socket;
        }

        @Override
        public void send(String line) {
            try {
                socket.send(line);
            } catch (WebsocketNotConnectedException e) {
                // Client déjà parti : onClose s'occupe du nettoyage.
            }
        }

        @Override
        public void close() {
            socket.close();
        }

        @Override
        public String describe() {
            return "ws " + socket.getRemoteSocketAddress();
        }
    }
}
