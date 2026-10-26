package com.chessserver.server;

import com.chessgame.utils.Move;
import com.chessserver.exceptions.GameFullException;
import com.chessserver.utils.Logger;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class GameSession implements Serializable {

    private String id;
    private String gameName;
    private String player1;
    private String player2;
    private transient PlayerManager playerManager;
    private boolean isFull = false;
    private GameManager gameManager;
    private int currentPlayer = 0;
    Semaphore semPartyStart; // semaphore to wait the beginning of the party, using nbPartyStart as a counter
    int nbPartyStart; // number of threads waiting for the party to start
    Semaphore semCurrentPlayed; // semaphore to wait that current player played sthg. CAUTION: the current thread MUST also use this semaphore
    int nbCurrentPlayed; // number of thread that are waiting for current player played
    Semaphore semEndParty; // semaphore to wait the end of the party
    int nbPartyEnd;
    private final Semaphore semCurrentPlayer = new Semaphore(1);

    public GameSession(String creatorPseudo, String gameName, PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.gameName = gameName;
        this.id = UUID.randomUUID().toString();
        this.player1 = creatorPseudo;
        semPartyStart = new Semaphore(0);
        semEndParty = new Semaphore(0);
    }

     public void waitPartyStart() {
        try {
            nbPartyStart += 1;
            if (nbPartyStart == 2) {
                semPartyStart.release(2);
            }
            else{
                Logger.info("Waiting for party to start");
            }
            semPartyStart.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
     }

     public void createGame() {
        gameManager = new GameManager();
        gameManager.initGame(player1, player2, gameName);
     }

     public void waitPartyEnd() {
        try {
            semEndParty.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
     }



    public synchronized void addPlayer(String playerPseudo) throws GameFullException {
        if (player2 == null) {
            waitPartyStart();
            player2 = playerPseudo;
            isFull = true;
            createGame();
        } else {
            throw new GameFullException("Partie complète");
        }
    }

    public boolean isFull() {
        return this.isFull;
    }

    public String getId() {
        return this.id;
    }

    public String getGameName() {
        return this.gameName;
    }

    public GameManager getGameManager() {return this.gameManager;}

     public synchronized void setNextPlayer() {
        currentPlayer = (currentPlayer+1)%2;
    }


    public String getOtherPlayer(String playerPseudo) {
        if (player1.equals(playerPseudo)) {
            return player2;
        } else {
            return player1;
        }
    }

    public int getNombreJoueur() {
        int res = 0;
        if (player1 != null) {
            res++;
        }
        if (player2 != null) {
            res++;
        }
        return res;
    }


    @Override
    public String toString(){
        return this.gameName + " " + this.getNombreJoueur() + "/2 joueurs";
    }

    public void waitTurn() {
        try {
            semCurrentPlayer.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error("Erreur lors de l'attente du tour du joueur : " + e.getMessage());
        }
    }

    public void endTurn() {
        semCurrentPlayer.release(); // Libère le tour pour le joueur suivant
    }

    public void sendMoveToplayer(Move move) {
        ClientHandler handler = currentPlayer == 0 ? playerManager.getPlayerHandler(player1) : playerManager.getPlayerHandler(player2);
        System.out.println(currentPlayer);
        System.out.println(handler.getPlayerPseudo());
        handler.sendMoveToPlayer(move);

    }
}

