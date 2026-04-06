package model;

import java.util.*;

public class Board {
    private final int gridSize; // ex: 3 (pontos)
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
        // cria as caixas baseadas nas linhas existentes
        for (int i = 0; i < gridSize - 1; i++) {
            for (int j = 0; j < gridSize - 1; j++) {
                Line top = getLine(i, j, i, j + 1);
                Line bottom = getLine(i + 1, j, i + 1, j + 1);
                Line left = getLine(i, j, i + 1, j);
                Line right = getLine(i, j + 1, i + 1, j + 1);
                boxes.add(new Box(new Line[]{top, bottom, left, right}));
            }
        }
    }

    public boolean makeMove(int x1, int y1, int x2, int y2, String playerNickname) {
        Line line = getLine(x1, y1, x2, y2);
        if (line == null || line.isOccupied()) return false;

        line.setOccupied(true, playerNickname);
        boolean closedAnyBox = false;

        for (Box box : boxes) {
            if (box.checkCompleted(playerNickname)) {
                closedAnyBox = true;
            }
        }
        return closedAnyBox; // Se true, o jogador joga outra vez 
    }

    // Métodos Auxiliares
    private void createLine(int x1, int y1, int x2, int y2) {
        Line l = new Line(new Dot(x1, y1), new Dot(x2, y2));
        lines.put(getLineKey(x1, y1, x2, y2), l);
    }

    private Line getLine(int x1, int y1, int x2, int y2) {
        // Tenta encontrar a linha independentemente da ordem dos pontos
        Line l = lines.get(getLineKey(x1, y1, x2, y2));
        if (l == null) l = lines.get(getLineKey(x2, y2, x1, y1));
        return l;
    }

    private String getLineKey(int x1, int y1, int x2, int y2) {
        return Math.min(x1,x2)+","+Math.min(y1,y2)+"-"+Math.max(x1,x2)+","+Math.max(y1,y2);
    }

    public boolean isGameOver() {
        return lines.values().stream().allMatch(Line::isOccupied);
    }
}