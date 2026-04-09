package controller;

import model.Board;
import model.Player;
import model.Line;
import server.ClientHandler;
import utils.PlayerDB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Random;
import java.util.List;

public class GameRoom {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Player playerModel1;
    private final Player playerModel2;
    private final Board board;
    private ClientHandler currentTurn;
    private String lastMoveCoords = "";

    public GameRoom(ClientHandler p1, ClientHandler p2, int gridSize) {
        this.player1 = p1;
        this.player2 = p2;
        this.playerModel1 = p1.getPlayer();
        this.playerModel2 = p2.getPlayer();
        this.board = new Board(gridSize);

        p1.setGameSession(this);
        p2.setGameSession(this);

        this.currentTurn = new Random().nextBoolean() ? p1 : p2;
        System.out.println("[GAME] Partida iniciada: " + p1.getNickname() + " vs " + p2.getNickname());

        broadcastGameStart();
    }

    private void broadcastGameStart() {
        // XML para o jogador 1
        Document doc1 = createBaseDocument();
        Element match1 = doc1.createElement("match");
        match1.setAttribute("start", "true");
        match1.setAttribute("opponent", player2.getNickname());
        match1.setAttribute("playerRole", "1");
        match1.setAttribute("size", String.valueOf(board.getGridSize()));
        doc1.getDocumentElement().appendChild(match1);
        player1.sendValidatedXML(doc1);

        // XML para o jogador 2
        Document doc2 = createBaseDocument();
        Element match2 = doc2.createElement("match");
        match2.setAttribute("start", "true");
        match2.setAttribute("opponent", player1.getNickname());
        match2.setAttribute("playerRole", "2");
        match2.setAttribute("size", String.valueOf(board.getGridSize()));
        doc2.getDocumentElement().appendChild(match2);
        player2.sendValidatedXML(doc2);

        broadcastGameState();
    }

    /**
     *
     * @param sender
     * @param line
     */
    public synchronized void handleMove(ClientHandler sender, Line line) {
        if (sender != currentTurn) {
            sendError(sender, "Não é o teu turno!");
            return;
        }

        if (line.isOccupied()) {
            sendError(sender, "Essa linha já foi traçada! Escolhe outra.");
            return; // retorna sem mudar o currentTurn, assim o jogador tenta outra vez
        }

        try {
            boolean closedBox = board.makeMove(line, sender.getPlayer());
            this.lastMoveCoords = line.toString();

            if (!closedBox) {
                currentTurn = (currentTurn == player1) ? player2 : player1;
            }

            broadcastGameState();

            if (board.isGameOver()) {
                broadcastGameOver();
            }
        } catch (Exception e) {
            sendError(sender, "Erro na jogada: " + e.getMessage());
        }
    }

    private void broadcastGameState() {
        Document doc = generateUpdateDocument();
        player1.sendValidatedXML(doc);
        player2.sendValidatedXML(doc);
    }

    /**
     * Converte o estado do jogo num documento para ser enviado aos clientes (XML)
     * @return
     */
    private Document generateUpdateDocument() {
        Document doc = createBaseDocument();
        Element update = doc.createElement("update");

        update.setAttribute("next", currentTurn.getNickname());
        update.setAttribute("scores", calculateScoresString());
        update.setAttribute("lastMove", lastMoveCoords);

        StringBuilder sb = new StringBuilder();
        int boxLimit = board.getGridSize() - 1;
        for (int i = 0; i < boxLimit; i++) {
            for (int j = 0; j < boxLimit; j++) {
                Player owner = board.getBoxOwner(i, j);
                if (owner != null) {
                    if (sb.length() > 0) sb.append("|");
                    sb.append(i).append(",").append(j).append(":").append(owner);
                }
            }
        }
        update.setAttribute("boxes", sb.toString());

        doc.getDocumentElement().appendChild(update);
        return doc;
    }

    private void broadcastGameOver() {
        int p1Score = board.getScore(player1.getPlayer());
        int p2Score = board.getScore(player2.getPlayer());

        String result;
        Player winner = null;
        Player loser = null;

        if (p1Score > p2Score) {
            result = "Vencedor: " + player1.getNickname();
            winner = playerModel1; loser = playerModel2;
        } else if (p2Score > p1Score) {
            result = "Vencedor: " + player2.getNickname();
            winner = playerModel2; loser = playerModel1;
        } else {
            result = "Empate!";
        }

        updatePlayerStats(winner, loser);

        Document doc = createBaseDocument();
        Element gameOver = doc.createElement("gameOver");
        gameOver.setAttribute("msg", result);
        doc.getDocumentElement().appendChild(gameOver);

        player1.sendValidatedXML(doc);
        player2.sendValidatedXML(doc);
    }

    private Document createBaseDocument() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().newDocument();
            Element root = doc.createElement("protocol");
            doc.appendChild(root);
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar documento DOM");
        }
    }

    private void sendError(ClientHandler player, String msg) {
        Document doc = createBaseDocument();
        Element resp = doc.createElement("response");
        resp.setAttribute("status", "fail");
        resp.setAttribute("msg", msg);
        doc.getDocumentElement().appendChild(resp);
        player.sendValidatedXML(doc);
    }

    private String calculateScoresString() {
        return String.format("%s: %d, %s: %d",
                player1.getNickname(), board.getScore(player1.getPlayer()),
                player2.getNickname(), board.getScore(player2.getPlayer()));
    }

    private void updatePlayerStats(Player winner, Player loser) {
        synchronized (PlayerDB.class) {
            if (winner != null) winner.addWin();
            if (loser  != null) loser.addLoss();
            List<Player> all = PlayerDB.load();
            for (Player p : all) {
                if (winner != null && p.getNickname().equals(winner.getNickname()))
                    p.setTotalWins(winner.getTotalWins());
                if (loser != null && p.getNickname().equals(loser.getNickname()))
                    p.setTotalLosses(loser.getTotalLosses());
            }
            PlayerDB.save(all);
        }
    }
}