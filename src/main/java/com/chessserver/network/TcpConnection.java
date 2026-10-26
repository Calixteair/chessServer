package com.chessserver.network;

import com.chessserver.server.ClientSession;
import com.chessserver.server.Lobby;
import com.chessserver.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Client connecté en TCP (client JavaFX) : une ligne de texte UTF-8 par message.
 * Chaque connexion est lue par son propre thread.
 */
public class TcpConnection implements Connection, Runnable {

    private final Socket socket;
    private final ClientSession session;
    private final PrintWriter out;

    public TcpConnection(Socket socket, Lobby lobby) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        this.session = new ClientSession(this, lobby);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                session.onMessage(line);
            }
        } catch (IOException e) {
            Logger.info("Connexion perdue (" + describe() + ") : " + e.getMessage());
        } finally {
            session.onClose();
            close();
        }
    }

    @Override
    public void send(String line) {
        synchronized (out) {
            out.println(line);
        }
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            Logger.warn("Fermeture du socket impossible : " + e.getMessage());
        }
    }

    @Override
    public String describe() {
        return "tcp " + socket.getRemoteSocketAddress();
    }
}
