package model;

public class Box {
    private final Line[] sides; // as 4 linhas que formam o quadrado
    private String ownerNickname; // armazena quem fechou a caixa (null se aberta)

    public Box(Line[] sides) {
        if (sides.length != 4) {
            throw new IllegalArgumentException("[BOX] Uma caixa deve ter exatamente 4 linhas.");
        }
        this.sides = sides;
        this.ownerNickname = null;
    }

    /**
     * Verifica se a caixa foi completada nesta jogada.
     * Se todas as linhas estiverem ocupadas e a caixa ainda não tiver dono,
     * atribui o dono e retorna true.
     */
    public boolean checkCompleted(String currentPlayerNickname) {
        if (ownerNickname != null) return false; // Já estava fechada antes

        for (Line line : sides) {
            if (!line.isOccupied()) {
                return false; // Se houver uma linha vazia, não está completa
            }
        }

        // Se chegou aqui, todas as linhas estão ocupadas
        this.ownerNickname = currentPlayerNickname;
        return true;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public boolean isClosed() {
        return ownerNickname != null;
    }
}