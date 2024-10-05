package com.chessserver.server;

import com.chessserver.exceptions.GameFullException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameRegistry {
    private List<GameSession> activeGames = new ArrayList<>();

    public synchronized GameSession createGame(String creatorPseudo) {
        GameSession game = new GameSession(creatorPseudo);
        activeGames.add(game);
        return game;
    }

    public synchronized List<GameSession> listAvailableGames() {
        return activeGames.stream()
                .filter(game -> !game.isFull()) // On ne liste que les parties avec de la place
                .collect(Collectors.toList());
    }

    public synchronized void removeGame(GameSession game) {
        activeGames.remove(game);
    }

    public synchronized GameSession joinGame(String gameId, String playerPseudo) throws GameFullException {
        for (GameSession game : activeGames) {
            if (game.getId().equals(gameId) && !game.isFull()) {
                game.addPlayer(playerPseudo);
                return game;
            }
        }
        throw new GameFullException("Partie complète ou introuvable");
    }
}
