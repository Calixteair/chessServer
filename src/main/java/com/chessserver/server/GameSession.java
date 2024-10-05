package com.chessserver.server;

import com.chessserver.exceptions.GameFullException;

import java.util.UUID;

public class GameSession {
    private String id;
    private String player1;
    private String player2;
    private boolean isFull = false;
    private GameManager gameManager;

    public GameSession(String creatorPseudo) {
        this.id = UUID.randomUUID().toString();
        this.player1 = creatorPseudo;
        this.gameManager = new GameManager();


    }

    public synchronized void addPlayer(String playerPseudo) throws GameFullException {
        if (isFull) {
            throw new GameFullException("Partie déjà complète");
        }
        if (player2 == null) {
            this.player2 = playerPseudo;
            isFull = true;
            gameManager.initGame(player1, player2);
        }
    }

    public boolean isFull() {
        return isFull;
    }

    public String getId() {
        return id;
    }
}

