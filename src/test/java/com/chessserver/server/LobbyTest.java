package com.chessserver.server;

import com.chessserver.network.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

    /** Connexion factice qui mémorise les lignes envoyées. */
    static class FakeConnection implements Connection {
        final List<String> sent = new ArrayList<>();
        boolean closed;

        @Override
        public synchronized void send(String line) {
            sent.add(line);
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public String describe() {
            return "fake";
        }

        synchronized String last(String prefix) {
            for (int i = sent.size() - 1; i >= 0; i--) {
                if (sent.get(i).startsWith(prefix)) {
                    return sent.get(i);
                }
            }
            return null;
        }
    }

    private Lobby lobby;
    private FakeConnection aliceConn;
    private FakeConnection bobConn;
    private ClientSession alice;
    private ClientSession bob;

    @BeforeEach
    void setUp() {
        lobby = new Lobby();
        aliceConn = new FakeConnection();
        bobConn = new FakeConnection();
        alice = new ClientSession(aliceConn, lobby);
        bob = new ClientSession(bobConn, lobby);
        alice.onMessage("CONNECT;alice");
        bob.onMessage("CONNECT;bob");
    }

    private String startGame() {
        alice.onMessage("CREATE_GAME;Partie du soir");
        String created = aliceConn.last("GAME_CREATED");
        assertNotNull(created);
        String id = created.split(";")[1];
        assertTrue(bobConn.last("GAMES").contains(id + ",Partie du soir,alice"), "liste poussée au lobby");
        bob.onMessage("JOIN_GAME;" + id);
        return id;
    }

    @Test
    void pseudoMustBeValidAndUnique() {
        FakeConnection conn = new FakeConnection();
        ClientSession other = new ClientSession(conn, lobby);
        other.onMessage("CONNECT;Alice");
        assertTrue(conn.last("ERROR").startsWith("ERROR;PSEUDO_TAKEN"));
        other.onMessage("CONNECT;a;b");
        assertTrue(conn.last("ERROR").startsWith("ERROR;INVALID_PSEUDO"));
        assertEquals("WELCOME;alice", aliceConn.last("WELCOME"));
    }

    @Test
    void commandsRequireConnection() {
        FakeConnection conn = new FakeConnection();
        new ClientSession(conn, lobby).onMessage("LIST_GAMES");
        assertTrue(conn.last("ERROR").startsWith("ERROR;NOT_CONNECTED"));
    }

    @Test
    void joiningStartsTheGameForBothPlayers() {
        String id = startGame();
        assertTrue(aliceConn.last("GAME_START").endsWith(";alice;bob;w"));
        assertTrue(bobConn.last("GAME_START").endsWith(";alice;bob;b"));
        assertTrue(aliceConn.last("STATE").startsWith("STATE;rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;ONGOING;-;0;-;;"));
        assertEquals("GAMES;", lobby.gamesMessage(), "la partie n'est plus listée");

        FakeConnection third = new FakeConnection();
        ClientSession carol = new ClientSession(third, lobby);
        carol.onMessage("CONNECT;carol");
        carol.onMessage("JOIN_GAME;" + id);
        assertTrue(third.last("ERROR").startsWith("ERROR;GAME_FULL"));
    }

    @Test
    void movesAreValidatedAndBroadcast() {
        startGame();
        bob.onMessage("MOVE;e7e5");
        assertTrue(bobConn.last("ERROR").startsWith("ERROR;NOT_YOUR_TURN"));
        alice.onMessage("MOVE;e2e5");
        assertTrue(aliceConn.last("ERROR").startsWith("ERROR;ILLEGAL_MOVE"));
        alice.onMessage("MOVE;e2e4");
        String state = bobConn.last("STATE");
        assertTrue(state.contains(" b KQkq e3 0 1;ONGOING;-;0;e2e4;e4;"), state);
        assertEquals(state, aliceConn.last("STATE"));
    }

    @Test
    void checkmateIsReported() {
        startGame();
        alice.onMessage("MOVE;f2f3");
        bob.onMessage("MOVE;e7e5");
        alice.onMessage("MOVE;g2g4");
        bob.onMessage("MOVE;d8h4");
        String[] state = aliceConn.last("STATE").split(";", -1);
        assertEquals("CHECKMATE", state[2]);
        assertEquals("b", state[3]);
        assertEquals("1", state[4]);
        assertEquals("f3 e5 g4 Qh4#", state[6]);
        assertEquals("", state[7]);
    }

    @Test
    void disconnectingDuringGameLosesByAbandon() {
        startGame();
        alice.onClose();
        String[] state = bobConn.last("STATE").split(";", -1);
        assertEquals("ABANDONED", state[2]);
        assertEquals("b", state[3]);
        assertEquals("OPPONENT_LEFT;alice", bobConn.last("OPPONENT_LEFT"));
        bob.onMessage("LEAVE_GAME");
        assertEquals("GAMES;", bobConn.last("GAMES"));
    }

    @Test
    void creatorLeavingRemovesWaitingGame() {
        alice.onMessage("CREATE_GAME;Test");
        alice.onMessage("LEAVE_GAME");
        assertEquals("GAMES;", lobby.gamesMessage());
    }
}
