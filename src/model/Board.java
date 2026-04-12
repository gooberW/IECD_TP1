package model;

import java.util.*;

public class Board {
    private final int gridSize;
    private final Map<String, Line> lines = new HashMap<>();
    private final List<Box> boxes = new ArrayList<>();

    public enum MoveResult { INVALID, CLOSED_BOX, NO_BOX_CLOSED }

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

    public MoveResult makeMove(Line line, Player player) {
        Line realLine = getLine(line.getDot1().getX(), line.getDot1().getY(),
                line.getDot2().getX(), line.getDot2().getY());

        if (realLine == null || realLine.isOccupied()) return MoveResult.INVALID;

        realLine.setOccupied(true);

        for (Box box : boxes) {
            if (box.checkCompleted(player)) return MoveResult.CLOSED_BOX;
        }
        return MoveResult.NO_BOX_CLOSED;
    }

    public boolean isGameOver() {
        long conquistadas = boxes.stream()
                .filter(box -> box.getOwner() != null)
                .count();

        int totalPossivel = (gridSize - 1) * (gridSize - 1);

        System.out.println("[DEBUG] Fim de Jogo? " + conquistadas + "/" + totalPossivel + " caixas preenchidas.");

        return conquistadas == totalPossivel;
    }

    public int getScore(Player player) {
        int score = 0;
        for (Box box : boxes) {
            if (box.getOwner() == player) {
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
        // a normalização da linha é feita no construtor
        Line temp = new Line(new Dot(x1, y1), new Dot(x2, y2));
        return temp.getDot1() + "-" + temp.getDot2();
    }

    public Player getBoxOwner(int row, int col) {
        for (Box b : boxes) {
            if (b.getRow() == row && b.getCol() == col) {
                return b.getOwner();
            }
        }
        return null;
    }

    public int getGridSize() { return gridSize; }
}