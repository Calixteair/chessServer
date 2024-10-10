package com.chessserver.server;

import com.chessserver.exceptions.GameFullException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameRegistry {

    //hashmap
    private Map<String, GameSession> activeGames = new HashMap<>();
    //private List<GameSession> activeGames = new ArrayList<>();

    public synchronized GameSession createGame(String creatorPseudo, String gameName) {
        GameSession game = new GameSession(creatorPseudo, gameName);
        activeGames.put(game.getId(), game);
        return game;
    }

    public synchronized List<GameSession> listAvailableGames() {
        //add une gameSession dans la liste
        return activeGames.values().stream().filter(game -> !game.isFull()).collect(Collectors.toList());
    }

    public synchronized void removeGame(GameSession game) {
        activeGames.remove(game);
    }

    public synchronized GameSession joinGame(String gameId, String playerPseudo) throws GameFullException {
        GameSession game = activeGames.get(gameId);
        if (game != null) {
            game.addPlayer(playerPseudo);
            return game;
        }
        throw new GameFullException("Partie complète ou introuvable");
    }

    public synchronized GameSession getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public synchronized void removeGame(String gameId) {
        activeGames.remove(gameId);
    }

    public synchronized boolean gameExists(String gameId) {
        return activeGames.containsKey(gameId);
    }


}
