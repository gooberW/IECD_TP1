package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma sala de jogos (lobby).
 * 
 * Gere a fila de espera de jogadores e cria novas partidas quando há jogadores suficientes nessa fila de espera.
 * 
 */
public class GameRoom {
    
    /** Lista de jogadores à espera de um oponente */
    private List<String> filaEspera;
    
    /** Lista de jogos ativos */
    private List<Game> jogosAtivos;
    
    /** Histórico de jogos terminados */
    private List<Game> historicoJogos;
    
    /** Tamanho padrão do tabuleiro */
    private int tamanhoTabuleiro;
    
    /**
     * Construtor da sala de jogos.
     * 
     * @param tamanhoTabuleiro Tamanho da grelha para os novos jogos (ex: 3, 5)
     */
    public GameRoom(int tamanhoTabuleiro) {
        this.filaEspera = new ArrayList<>();
        this.jogosAtivos = new ArrayList<>();
        this.historicoJogos = new ArrayList<>();
        this.tamanhoTabuleiro = tamanhoTabuleiro;
    }
    
    /**
     * Adiciona um jogador à fila de espera.
     * Se houver pelo menos 2 jogadores na fila, cria automaticamente um novo jogo.
     * 
     * @param nomeJogador Nome do jogador a entrar na fila
     * @return O jogo criado (se houver pares), ou null se ainda à espera
     */
    public Game adicionarJogador(String nomeJogador) {
        System.out.println(nomeJogador + " entrou na fila de espera.");
        filaEspera.add(nomeJogador);
        
        // Verifica se há pelo menos 2 jogadores para formar um jogo
        if (filaEspera.size() >= 2) {
            String jogador1 = filaEspera.remove(0);
            String jogador2 = filaEspera.remove(0);
            return criarJogo(jogador1, jogador2);
        }
        
        return null;  // Aguarda mais jogadores
    }
    
    /**
     * Cria um novo jogo entre dois jogadores.
     * 
     * @param nomeJogador1 Primeiro jogador
     * @param nomeJogador2 Segundo jogador
     * @return O novo jogo criado
     */
    private Game criarJogo(String nomeJogador1, String nomeJogador2) {
        System.out.println(" Novo jogo criado entre " + nomeJogador1 + " e " + nomeJogador2 + "!");
        
        Game novoJogo = new Game(nomeJogador1, nomeJogador2, tamanhoTabuleiro);
        jogosAtivos.add(novoJogo);
        return novoJogo;
    }
    
    /**
     * Remove um jogo da lista de ativos (quando termina) e adiciona ao histórico.
     * 
     * @param jogo O jogo a terminar
     */
    public void terminarJogo(Game jogo) {
        if (jogosAtivos.remove(jogo)) {
            historicoJogos.add(jogo);
            System.out.println(" Jogo terminado e movido para o histórico.");
        }
    }
    
    /**
     * Obtém a lista de jogos ativos.
     * 
     * @return Lista de jogos em curso
     */
    public List<Game> getJogosAtivos() {
        return new ArrayList<>(jogosAtivos);
    }
    
    /**
     * Obtém o número de jogadores na fila de espera.
     * 
     * @return Tamanho da fila
     */
    public int getTamanhoFila() {
        return filaEspera.size();
    }
    
    /**
     * Obtém a lista de jogadores em espera.
     * 
     * @return Lista de nomes dos jogadores à espera
     */
    public List<String> getFilaEspera() {
        return new ArrayList<>(filaEspera);
    }
    
    /**
     * Obtém estatísticas da sala de espera.
     * 
     * @return String com resumo da atividade da sala de espera
     */
    public String getEstatisticas() {
        return String.format(
            " Estatísticas da Sala:\n" +
            "   - Jogos ativos: %d\n" +
            "   - Jogos terminados: %d\n" +
            "   - Jogadores em espera: %d\n" +
            "   - Tamanho do tabuleiro: %dx%d",
            jogosAtivos.size(),
            historicoJogos.size(),
            filaEspera.size(),
            tamanhoTabuleiro,
            tamanhoTabuleiro
        );
    }
}
