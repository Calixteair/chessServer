package com.chessserver.server;

import com.chessserver.network.Protocol;
import com.chessserver.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Registre partagé des joueurs connectés et des parties, commun aux clients TCP et WebSocket.
 *
 * Verrouillage : le lobby peut prendre le verrou d'une {@link GameRoom}, jamais l'inverse.
 */
public class Lobby {

    private static final Pattern PSEUDO = Pattern.compile("[A-Za-z0-9_-]{1,20}");
    private static final Pattern GAME_NAME = Pattern.compile("[\\p{L}\\p{N} _'-]{1,24}");

    private final Map<String, ClientSession> players = new HashMap<>();
    private final Map<String, GameRoom> rooms = new LinkedHashMap<>();

    public synchronized void register(ClientSession session, String pseudo) throws ProtocolException {
        if (pseudo == null || !PSEUDO.matcher(pseudo).matches()) {
            throw new ProtocolException(Protocol.ERR_INVALID_PSEUDO,
                    "Pseudo : 1 à 20 caractères parmi lettres, chiffres, _ et -");
        }
        if (players.containsKey(pseudo.toLowerCase())) {
            throw new ProtocolException(Protocol.ERR_PSEUDO_TAKEN, "Le pseudo « " + pseudo + " » est déjà pris");
        }
        players.put(pseudo.toLowerCase(), session);
    }

    public synchronized void unregister(ClientSession session) {
        players.values().remove(session);
    }

    public GameRoom createGame(ClientSession creator, String name) throws ProtocolException {
        String trimmed = name == null ? "" : name.trim();
        if (!GAME_NAME.matcher(trimmed).matches()) {
            throw new ProtocolException(Protocol.ERR_INVALID_GAME_NAME,
                    "Nom de partie : 1 à 24 caractères (lettres, chiffres, espaces, _ ' -)");
        }
        GameRoom room;
        synchronized (this) {
            room = new GameRoom(newGameId(), trimmed, creator);
            rooms.put(room.getId(), room);
        }
        creator.enterRoom(room);
        Logger.info(creator.getPseudo() + " crée la partie « " + trimmed + " » (" + room.getId() + ")");
        broadcastGames();
        return room;
    }

    private String newGameId() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8);
        } while (rooms.containsKey(id));
        return id;
    }

    public GameRoom joinGame(ClientSession player, String gameId) throws ProtocolException {
        GameRoom room;
        synchronized (this) {
            room = rooms.get(gameId);
            if (room == null) {
                throw new ProtocolException(Protocol.ERR_GAME_NOT_FOUND, "Partie introuvable");
            }
            if (!room.tryJoin(player)) {
                throw new ProtocolException(Protocol.ERR_GAME_FULL, "Cette partie a déjà commencé");
            }
        }
        player.enterRoom(room);
        Logger.info(player.getPseudo() + " rejoint « " + room.getName() + " »");
        room.announceStart();
        broadcastGames();
        return room;
    }

    public void leaveGame(ClientSession player, GameRoom room) {
        room.leave(player);
        synchronized (this) {
            if (room.isEmpty()) {
                rooms.remove(room.getId());
            }
        }
        broadcastGames();
    }

    /** Liste des parties en attente d'un adversaire. */
    public synchronized String gamesMessage() {
        List<String[]> waiting = new ArrayList<>();
        for (GameRoom room : rooms.values()) {
            String[] entry = room.lobbyEntry();
            if (entry != null) {
                waiting.add(entry);
            }
        }
        return Protocol.games(waiting);
    }

    /** Pousse la liste à jour à tous les joueurs qui ne sont pas en partie. */
    private void broadcastGames() {
        String message;
        List<ClientSession> idle = new ArrayList<>();
        synchronized (this) {
            message = gamesMessage();
            for (ClientSession s : players.values()) {
                if (!s.isInRoom()) {
                    idle.add(s);
                }
            }
        }
        idle.forEach(s -> s.send(message));
    }
}
