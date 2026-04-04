package view;

import model.Board;
import model.Game;
import model.GameRoom;
import java.util.Scanner;

/**
 * Nesta primeira parte do trabalho prático, o jogo Pontos e Caixas é executado na consola do eclipse. Contudo, na segunda parte do trabalho prático será 
 executado na web.
 * 
 * Responsável por mostrar o tabuleiro, receber input do jogador e exibir mensagens de estado do jogo.
 * 
 */
public class ConsoleView {
    
    private Scanner scanner;
    
    /**
     * Construtor da vista de consola.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Inicia o menu principal da aplicação.
     */
    public void iniciarMenuPrincipal() {
        System.out.println("");
        System.out.println("Jogo pontos e caixas:");
        System.out.println("");
        System.out.println(" - Criar nova sala");
        System.out.println(" - Entrar na sala existente");
        System.out.println(" - Ver estatísticas da sala");
        System.out.println(" - Sair");
        System.out.print("\n Escolha uma opção: ");
    }
    
    /**
     * Mostra o tabuleiro atual no ecrã.
     * 
     * @param board Tabuleiro a mostrar
     */
    public void mostrarTabuleiro(Board board) {
        int size = board.getGridSize();
        int[][] boxes = board.getBoxes();
        
        System.out.println("\n   Tabuleiro:");
        
        // Para cada linha de pontos
        for (int i = 0; i <= size; i++) {
            // Mostra linhas horizontais
            if (i < size) {
                System.out.print("  ");
                for (int j = 0; j <= size; j++) {
                    System.out.print(".");  // Ponto
                    if (j < size) {
                        if (board.hasHorizontalLine(i, j)) {
                            System.out.print("───");
                        } else {
                            System.out.print("   ");
                        }
                    }
                }
                System.out.println();
                
                // Mostra linhas verticais e caixas
                System.out.print("  ");
                for (int j = 0; j <= size; j++) {
                    if (j < size) {
                        if (board.hasVerticalLine(i, j)) {
                            System.out.print("│");
                        } else {
                            System.out.print(" ");
                        }
                        
                        // Mostra dono da caixa
                        if (boxes[i][j] == 0) {
                            System.out.print(" A ");
                        } else if (boxes[i][j] == 1) {
                            System.out.print(" B ");
                        } else {
                            System.out.print("   ");
                        }
                    }
                }
                System.out.println();
            } else {
                // Última linha (apenas pontos)
                System.out.print("  ");
                for (int j = 0; j <= size; j++) {
                    System.out.print(".");
                    if (j < size) {
                        if (board.hasHorizontalLine(i, j)) {
                            System.out.print("───");
                        } else {
                            System.out.print("   ");
                        }
                    }
                }
                System.out.println();
            }
        }
        System.out.println();
    }
    
    /**
     * Solicita uma jogada ao jogador.
     * 
     * @param nomeJogador Nome do jogador que vai jogar
     * @return Array com [linha, coluna, orientacao] onde orientacao: "H" ou "V"
     */
    public int[] solicitarJogada(String nomeJogador) {
        System.out.println("\n É a vez de: " + nomeJogador);
        System.out.print(" Linha (0 a 2): ");
        int linha = scanner.nextInt();
        System.out.print(" Coluna (0 a 2): ");
        int coluna = scanner.nextInt();
        System.out.print(" Orientação (H - horizontal / V - vertical): ");
        String orientacao = scanner.next().toUpperCase();
        
        boolean horizontal = orientacao.equals("H");
        return new int[]{linha, coluna, horizontal ? 1 : 0}; // 1=horizontal, 0=vertical
    }
    
    /**
     * Mostra uma mensagem de jogada inválida.
     */
    public void mostrarJogadaInvalida() {
        System.out.println(" Jogada inválida! Tente novamente.");
    }
    
    /**
     * Mostra uma mensagem de bónus (jogada extra).
     */
    public void mostrarBonus() {
        System.out.println("Bónus! Fechou uma caixa - joga novamente!");
    }
    
    /**
     * Mostra o resultado final do jogo.
     * 
     * @param resultado String com o resultado
     */
    public void mostrarResultado(String resultado) {
        System.out.println("Fim do jogo!");
        System.out.println(resultado);
    }
    
    /**
     * Mostra uma mensagem genérica.
     * 
     * @param mensagem Mensagem a exibir
     */
    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }
    
    /**
     * Mostra as estatísticas da sala.
     * 
     * @param gameRoom Sala de jogos
     */
    public void mostrarEstatisticas(GameRoom gameRoom) {
        System.out.println("\n" + gameRoom.getEstatisticas());
        System.out.print("\nPressione Enter para continuar...");
        scanner.nextLine();
        scanner.nextLine();
    }
    
    /**
     * Exemplo de execução de um jogo completo entre dois jogadores.
     * 
     * @param game Jogo a executar
     */
    public void executarJogo(Game game) {
        System.out.println("\n Novo Jogo: " + game.getJogador0() + " vs " + game.getJogador1() );
        
        while (game.isAtivo()) {
            mostrarTabuleiro(game.getBoard());
            
            boolean jogadaValida = false;
            while (!jogadaValida) {
                int[] jogada = solicitarJogada(game.getJogadorAtual());
                int linha = jogada[0];
                int coluna = jogada[1];
                boolean horizontal = (jogada[2] == 1);
                
                jogadaValida = game.fazerJogada(linha, coluna, horizontal);
                
                if (!jogadaValida) {
                    mostrarJogadaInvalida();
                } else {
                    // Verifica se foi bónus
                    // Nota: Para simplificar, assumimos que se a jogada foi válida e o jogador não mudou, houve bónus
                    if (game.getJogadorAtualNumero() == 0 && game.getJogadorAtual().equals(game.getJogador0())) {
                        // O Jogador continua (bónus)
                        if (!game.isAtivo()) break;
                        mostrarBonus();
                    }
                }
            }
        }
        
        mostrarTabuleiro(game.getBoard());
        String resultado = game.verificarFimJogo();
        mostrarResultado(resultado);
    }
    
    /**
     * Fecha o scanner.
     */
    public void fechar() {
        scanner.close();
    }
}
