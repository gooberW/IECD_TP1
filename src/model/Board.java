package model;

import java.util.*;

public class Board {
    private final int gridSize;
    private final Map<String, Line> lines = new HashMap<>();
    private final List<Box> boxes = new ArrayList<>();

    public Board(int gridSize) {
        this.gridSize = gridSize;
        generateGraph();
    }

    private void generateGraph() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (j < gridSize - 1) createLine(i, j, i, j + 1); // Horizontal
                if (i < gridSize - 1) createLine(i, j, i + 1, j); // Vertical
            }
        }

        for (int i = 0; i < gridSize - 1; i++) {
            for (int j = 0; j < gridSize - 1; j++) {
                Line top = getLine(i, j, i, j + 1);
                Line bottom = getLine(i + 1, j, i + 1, j + 1);
                Line left = getLine(i, j, i + 1, j);
                Line right = getLine(i, j + 1, i + 1, j + 1);
                boxes.add(new Box(new Line[]{top, bottom, left, right}, i, j));
            }
        }
    }

    public boolean makeMove(int x1, int y1, int x2, int y2, String playerNickname) {
        Line line = getLine(x1, y1, x2, y2);

        if (line == null || line.isOccupied()) return false;

        line.setOccupied(true, playerNickname);
        boolean closedAnyBox = false;

        for (Box box : boxes) {
            // checkCompleted deve retornar true apenas se a caixa ACABOU de ser fechada
            if (box.checkCompleted(playerNickname)) {
                closedAnyBox = true;
            }
        }
        return closedAnyBox;
    }

    public boolean isLineOccupied(int x1, int y1, int x2, int y2) {
        Line l = getLine(x1, y1, x2, y2);
        return l != null && l.isOccupied();
    }

    public boolean isGameOver() {
        long conquistadas = boxes.stream()
                .filter(box -> box.getOwnerNickname() != null)
                .count();

        int totalPossivel = (gridSize - 1) * (gridSize - 1);

        System.out.println("[DEBUG] Fim de Jogo? " + conquistadas + "/" + totalPossivel + " caixas preenchidas.");

        return conquistadas == totalPossivel;
    }

    public int getScore(String playerNickname) {
        int score = 0;
        for (Box box : boxes) {
            if (playerNickname.equals(box.getOwnerNickname())) {
                score++;
            }
        }
        return score;
    }

    private void createLine(int x1, int y1, int x2, int y2) {
        Line l = new Line(new Dot(x1, y1), new Dot(x2, y2));
        lines.put(getLineKey(x1, y1, x2, y2), l);
    }

    private Line getLine(int x1, int y1, int x2, int y2) {
        // getLineKey já lida com a normalização (min/max), por isso a ordem x1/x2 não importa
        return lines.get(getLineKey(x1, y1, x2, y2));
    }

    private String getLineKey(int x1, int y1, int x2, int y2) {
        // garante que (0,0)-(0,1) e (0,1)-(0,0) resultam na mesma chave "0,0-0,1"
        int startX = Math.min(x1, x2);
        int startY = Math.min(y1, y2);
        int endX = Math.max(x1, x2);
        int endY = Math.max(y1, y2);
        return startX + "," + startY + "-" + endX + "," + endY;
    }

    public String getBoxOwner(int row, int col) {
        for (Box b : boxes) {
            if (b.getRow() == row && b.getCol() == col) {
                return b.getOwnerNickname();
            }
        }
        return null;
    }

    public int getGridSize() { return gridSize; }
}