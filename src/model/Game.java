package model;

/**
 * Representa uma partida do jogo Pontos e Caixas.
 * 
 * Gere o estado do jogo, os turnos dos jogadores, as jogadas e a lógica da obtenção do bónus (jogada extra ao fechar caixa).
 * 
 */
public class Game {
    
    /** Jogador 0 (primeiro jogador) */
    private String jogador0;
    
    /** Jogador 1 (segundo jogador) */
    private String jogador1;
    
    /** Tabuleiro do jogo */
    private Board board;
    
    /** Jogador atual (0 ou 1) */
    private int jogadorAtual;
    
    /** Indica se o jogo está ativo */
    private boolean ativo;
    
    /**
     * Construtor da partida.
     * 
     * @param nomeJogador0 Nome do primeiro jogador
     * @param nomeJogador1 Nome do segundo jogador
     * @param tamanhoTabuleiro Tamanho da grelha (ex: 3 para tabuleiro 3x3)
     */
    public Game(String nomeJogador0, String nomeJogador1, int tamanhoTabuleiro) {
        this.jogador0 = nomeJogador0;
        this.jogador1 = nomeJogador1;
        this.board = new Board(tamanhoTabuleiro);
        this.jogadorAtual = 0;  // Jogador 0 começa
        this.ativo = true;
    }
    
    /**
     * Executa uma jogada no tabuleiro.
     * 
     * @param linha      Linha onde desenhar 
     * @param coluna     Coluna onde desenhar 
     * @param horizontal true para linha horizontal, false para vertical
     * @return true se a jogada foi válida e executada, false caso contrário
     */
    public boolean fazerJogada(int linha, int coluna, boolean horizontal) {
        if (!ativo) {
            return false;  // Jogo já terminou
        }
        
        // Tenta desenhar a linha
        boolean fechouCaixa = board.desenharLinha(linha, coluna, horizontal, jogadorAtual);
        
        if (fechouCaixa) {
            // Regra de bónus: jogador joga novamente
            return true;
        } else {
            // Muda para o próximo jogador
            mudarJogador();
            return true;
        }
    }
    
    /**
     * Verifica se o jogo terminou e determina o vencedor.
     * 
     * @return String com o resultado ("Vitória do Jogador0", "Vitória do Jogador1", "Empate", ou null se o jogo não terminou)
     */
    public String verificarFimJogo() {
        if (!board.jogoTerminado()) {
            return null;  // Jogo ainda não terminou
        }
        
        ativo = false;
        int[] pontos = board.calcularPontuacao();
        
        if (pontos[0] > pontos[1]) {
            return "Vitória de " + jogador0 + "! Pontuação: " + pontos[0] + " a " + pontos[1];
        } else if (pontos[1] > pontos[0]) {
            return "Vitória de " + jogador1 + "! Pontuação: " + pontos[1] + " a " + pontos[0];
        } else {
            return "Empate! Ambos com " + pontos[0] + " caixas";
        }
    }
    
    /**
     * Muda o jogador atual.
     */
    private void mudarJogador() {
        jogadorAtual = (jogadorAtual == 0) ? 1 : 0;
    }
    
    /**
     * Obtém o nome do jogador atual.
     * 
     * @return Nome do jogador que deve jogar agora
     */
    public String getJogadorAtual() {
        return jogadorAtual == 0 ? jogador0 : jogador1;
    }
    
    /**
     * Obtém o número do jogador atual (0 ou 1).
     * 
     * @return Número do jogador atual
     */
    public int getJogadorAtualNumero() {
        return jogadorAtual;
    }
    
    /**
     * Verifica se o jogo está ativo.
     * 
     * @return true se jogo ativo, false se terminado
     */
    public boolean isAtivo() {
        return ativo;
    }
    
    /**
     * Obtém o tabuleiro.
     * 
     * @return objeto Board
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Obtém o nome do jogador 0.
     * 
     * @return nome do jogador 0
     */
    public String getJogador0() {
        return jogador0;
    }
    
    /**
     * Obtém o nome do jogador 1.
     * 
     * @return nome do jogador 1
     */
    public String getJogador1() {
        return jogador1;
    }
}
