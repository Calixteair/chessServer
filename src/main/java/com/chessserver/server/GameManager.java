package com.chessserver.server;

import com.chessgame.model.Board;
import com.chessgame.model.ChessGame;
import com.chessgame.model.Player;
import com.chessgame.utils.Move;

import java.io.Serializable;

public class GameManager implements Serializable {



    private String GameName;
    private ChessGame chessGame;

    public void initGame(String player1, String player2, String gameName) {
        this.chessGame = new ChessGame();
        this.GameName = gameName + " : " + player1 + " vs " + player2;
        Player whitePlayer = new Player(true, chessGame.getBoard(), player1);
        Player blackPlayer = new Player(false, chessGame.getBoard(), player2);
        this.chessGame.setWhitePlayer(whitePlayer);
        this.chessGame.setBlackPlayer(blackPlayer);
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

    public Board getBoard() {
        return chessGame.getBoard();
    }

    //implement serialization
    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        out.writeObject(GameName);
        out.writeObject(chessGame);
    }


}
