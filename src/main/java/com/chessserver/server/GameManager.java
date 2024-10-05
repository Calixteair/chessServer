package com.chessserver.server;

import com.chessgame.model.Board;
import com.chessgame.model.ChessGame;
import com.chessgame.utils.Move;

public class GameManager {



    private String GameName;
    private ChessGame chessGame;

    public void initGame(String player1, String player2) {
        this.chessGame = new ChessGame();
        this.GameName = player1 + " vs " + player2;

    }


    public void startGame() {
        while (!chessGame.isGameOver()) {
            // donner le plateau a chaque joueur


            chessGame.getCurrentPlayer().getValidMoves();
            // ask for move


            chessGame.switchPlayer();
        }
    }

    public void endGame() {
    }

    public void askForMove() {
        // ask for move
    }

    public void movePiece(Move move) {
        chessGame.getCurrentPlayer().movePiece(move.getStartX(), move.getStartY(), move.getEndX(), move.getEndY());
    }


}
