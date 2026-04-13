package model;

public class Box {
    private final Line[] sides; // as 4 linhas que formam o quadrado
    private Player owner; // armazena quem fechou a caixa (null se aberta)
    private int row, col;

    public Box(Line[] sides, int row, int col) {
        if (sides.length != 4) {
            throw new IllegalArgumentException("[BOX] Uma caixa deve ter exatamente 4 linhas.");
        }
        this.sides = sides;
        this.owner = null;

        this.col = col;
        this.row = row;
    }

    /**
     * Verifica se a caixa foi completada nesta jogada.
     * Se todas as linhas estiverem ocupadas e a caixa ainda não tiver dono,
     * atribui o dono e retorna true.
     */
    public boolean checkCompleted(Player currentPlayer) {
        if (owner != null) return false; // Já estava fechada antes

        if(isClosed()) {
            this.owner = currentPlayer;
            return true;
        } else {
            return false;
        }
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isClosed() {
        for (Line line : sides) {
            if (!line.isOccupied()) {
                return false;
            }
        }
        return true;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol(){
        return this.col;
    }
}