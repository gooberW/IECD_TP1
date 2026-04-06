package model;

import server.ClientHandler;
import utils.PlayerDB;
import java.util.Random;
import java.util.List;

public class GameRoom {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Board board;
    private ClientHandler currentTurn;
    private String lastMoveCoords = "";

    public GameRoom(ClientHandler p1, ClientHandler p2, int gridSize) {
        this.player1 = p1;
        this.player2 = p2;
        this.board = new Board(gridSize);

        p1.setGameSession(this);
        p2.setGameSession(this);

        this.currentTurn = new Random().nextBoolean() ? p1 : p2;
        System.out.println("[GAME] Partida iniciada: " + p1.getNickname() + " vs " + p2.getNickname());

        broadcastGameStart();
    }

    private void broadcastGameStart() {
        player1.sendXML("<protocol><match start='true' opponent='" + player2.getNickname() + "' playerRole='1'/></protocol>");
        player2.sendXML("<protocol><match start='true' opponent='" + player1.getNickname() + "' playerRole='2'/></protocol>");
        broadcastGameState();
    }

    public synchronized void handleMove(ClientHandler sender, int x1, int y1, int x2, int y2) {
        if (sender != currentTurn) {
            sender.sendXML("<protocol><response status='fail' msg='Nao e o teu turno!'/></protocol>");
            return;
        }

        boolean closedBox = board.makeMove(x1, y1, x2, y2, sender.getNickname());
        this.lastMoveCoords = String.format("%d,%d-%d,%d", x1, y1, x2, y2);

        if (!closedBox) {
            currentTurn = (currentTurn == player1) ? player2 : player1;
        }

        broadcastGameState();

        if (board.isGameOver()) {
            broadcastGameOver();
        }
    }

    private void broadcastGameState() {
        String updateXML = generateUpdateXML();
        player1.sendXML(updateXML);
        player2.sendXML(updateXML);
    }

    private String generateUpdateXML() {
        int p1Score = board.getScore(player1.getNickname());
        int p2Score = board.getScore(player2.getNickname());

        String scores = String.format("%s: %d, %s: %d",
                player1.getNickname(), p1Score,
                player2.getNickname(), p2Score);

        return "<protocol><update next='" + currentTurn.getNickname() + "' " +
                "scores='" + scores + "' lastMove='" + lastMoveCoords + "'/></protocol>";
    }

    private void broadcastGameOver() {
        int p1Score = board.getScore(player1.getNickname());
        int p2Score = board.getScore(player2.getNickname());

        String result;
        String winner = null;
        String loser = null;

        if (p1Score > p2Score) {
            result = "Vencedor: " + player1.getNickname();
            winner = player1.getNickname(); loser = player2.getNickname();
        } else if (p2Score > p1Score) {
            result = "Vencedor: " + player2.getNickname();
            winner = player2.getNickname(); loser = player1.getNickname();
        } else {
            result = "Empate!";
        }

        updatePlayerStats(winner, loser);

        String finalXML = "<protocol><gameOver msg='" + result + "'/></protocol>";
        player1.sendXML(finalXML);
        player2.sendXML(finalXML);
    }

    private void updatePlayerStats(String winner, String loser) {
        if (winner == null && loser == null) return;

        synchronized (PlayerDB.class) {
            List<Player> players = PlayerDB.load();
            for (Player p : players) {
                if (p.getNickname().equals(winner)) p.setTotalWins(p.getTotalWins() + 1);
                if (p.getNickname().equals(loser)) p.setTotalLosses(p.getTotalLosses() + 1);
            }
            PlayerDB.save(players);
        }
    }
}