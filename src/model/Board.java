package model;

/**
 * Representa o tabuleiro do jogo Pontos e Caixas.
 * 
 * O tabuleiro é composto por uma grelha de pontos (ex: 3x3, 5x5).
 * As linhas podem ser desenhadas entre pontos adjacentes (horizontais ou verticais).
 * Quando uma linha completa o 4º lado de uma caixa, essa caixa é conquistada.
 * 
 */
public class Board {
    
    /** Tamanho da grelha (ex: 3 para uma grelha 3x3) */
    private int gridSize;
    
    /** Linhas horizontais: [linha][coluna] - true se já desenhada */
    private boolean[][] horizontalLines;
    
    /** Linhas verticais: [linha][coluna] - true se já desenhada */
    private boolean[][] verticalLines;
    
    /** Dono de cada caixa: -1 = vazia, 0 = Jogador A, 1 = Jogador B */
    private int[][] boxes;
    
    /**
     * Construtor do tabuleiro.
     * 
     * @param gridSize Dimensão da grelha (ex: 3 para tabuleiro 3x3 pontos, que resulta em 2x2 caixas)
     */
    public Board(int gridSize) {
        this.gridSize = gridSize;
        
        // Inicializa matriz de linhas horizontais (gridSize+1 linhas, gridSize colunas)
        this.horizontalLines = new boolean[gridSize + 1][gridSize];
        
        // Inicializa matriz de linhas verticais (gridSize linhas, gridSize+1 colunas)
        this.verticalLines = new boolean[gridSize][gridSize + 1];
        
        // Inicializa matriz de caixas (gridSize x gridSize)
        this.boxes = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                boxes[i][j] = -1;  // -1 significa caixa vazia
            }
        }
    }
    
    /**
     * Desenha uma linha no tabuleiro.
     * 
     * @param linha      Índice da linha (0-based)
     * @param coluna     Índice da coluna (0-based)
     * @param horizontal true para linha horizontal, false para vertical
     * @param jogador    Número do jogador (0 ou 1)
     * @return true se a jogada fechou pelo menos uma caixa (dá direito a bónus), false caso contrário
     */
    public boolean desenharLinha(int linha, int coluna, boolean horizontal, int jogador) {
        // Valida se a linha já existe
        if (horizontal) {
            if (horizontalLines[linha][coluna]) {
                return false;  // Linha já desenhada
            }
            horizontalLines[linha][coluna] = true;
        } else {
            if (verticalLines[linha][coluna]) {
                return false;  // Linha já desenhada
            }
            verticalLines[linha][coluna] = true;
        }
        
        // Verifica se alguma caixa foi completada
        boolean fechouCaixa = false;
        
        if (horizontal) {
            // Verifica caixa acima (se linha > 0)
            if (linha > 0 && verificarCaixa(linha - 1, coluna)) {
                boxes[linha - 1][coluna] = jogador;
                fechouCaixa = true;
            }
            // Verifica caixa abaixo (se linha < gridSize)
            if (linha < gridSize && verificarCaixa(linha, coluna)) {
                boxes[linha][coluna] = jogador;
                fechouCaixa = true;
            }
        } else {
            // Verifica caixa à esquerda (se coluna > 0)
            if (coluna > 0 && verificarCaixa(linha, coluna - 1)) {
                boxes[linha][coluna - 1] = jogador;
                fechouCaixa = true;
            }
            // Verifica caixa à direita (se coluna < gridSize)
            if (coluna < gridSize && verificarCaixa(linha, coluna)) {
                boxes[linha][coluna] = jogador;
                fechouCaixa = true;
            }
        }
        
        return fechouCaixa;
    }
    
    /**
     * Verifica se uma caixa específica está completa (tem os 4 lados desenhados).
     * 
     * @param linhaCaixa  Linha da caixa 
     * @param colunaCaixa Coluna da caixa 
     * @return true se a caixa está completa, false caso contrário
     */
    private boolean verificarCaixa(int linhaCaixa, int colunaCaixa) {
        // Caixa já tem dono?
        if (boxes[linhaCaixa][colunaCaixa] != -1) {
            return false;
        }
        
        // Verifica linha superior (horizontal)
        boolean cima = horizontalLines[linhaCaixa][colunaCaixa];
        
        // Verifica linha inferior (horizontal)
        boolean baixo = horizontalLines[linhaCaixa + 1][colunaCaixa];
        
        // Verifica linha esquerda (vertical)
        boolean esquerda = verticalLines[linhaCaixa][colunaCaixa];
        
        // Verifica linha direita (vertical)
        boolean direita = verticalLines[linhaCaixa][colunaCaixa + 1];
        
        return cima && baixo && esquerda && direita;
    }
    
    /**
     * Verifica se o jogo terminou (todas as linhas possíveis já foram desenhadas).
     * 
     * @return true se o jogo terminou, false caso contrário
     */
    public boolean jogoTerminado() {
        // Verifica linhas horizontais
        for (int i = 0; i <= gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (!horizontalLines[i][j]) {
                    return false;  // Ainda há linha horizontal por desenhar
                }
            }
        }
        
        // Verifica linhas verticais
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j <= gridSize; j++) {
                if (!verticalLines[i][j]) {
                    return false;  // Ainda há linha vertical por desenhar
                }
            }
        }
        
        return true;
    }
    
    /**
     * Calcula a pontuação atual de cada jogador.
     * 
     * @return Array com 2 posições: [pontosJogador0, pontosJogador1]
     */
    public int[] calcularPontuacao() {
        int[] pontos = new int[2];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (boxes[i][j] == 0) {
                    pontos[0]++;
                } else if (boxes[i][j] == 1) {
                    pontos[1]++;
                }
            }
        }
        return pontos;
    }
    
    /**
     * Obtém o tamanho da grelha.
     * 
     * @return tamanho da grelha
     */
    public int getGridSize() {
        return gridSize;
    }
    
    /**
     * Obtém a matriz de caixas (para visualização).
     * 
     * @return matriz de caixas
     */
    public int[][] getBoxes() {
        return boxes;
    }
    
    /**
     * Verifica se uma linha horizontal já foi desenhada.
     * 
     * @param linha  Índice da linha
     * @param coluna Índice da coluna
     * @return true se a linha já existe
     */
    public boolean hasHorizontalLine(int linha, int coluna) {
        return horizontalLines[linha][coluna];
    }
    
    /**
     * Verifica se uma linha vertical já foi desenhada.
     * 
     * @param linha  Índice da linha
     * @param coluna Índice da coluna
     * @return true se a linha já existe
     */
    public boolean hasVerticalLine(int linha, int coluna) {
        return verticalLines[linha][coluna];
    }
}
