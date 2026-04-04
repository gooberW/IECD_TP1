import model.Game;
import model.GameRoom;
import view.ConsoleView;

/**
 * Classe principal para testar o jogo Pontos e Caixas.
 * 
 */
public class Main {
    
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        GameRoom sala = new GameRoom(3);  // Tabuleiro 3x3
        
        boolean continuar = true;
        
        while (continuar) {
            view.iniciarMenuPrincipal();
            int opcao = lerInteiro();
            
            switch (opcao) {
                case 1:
                    // Criar nova sala (jogo local)
                    System.out.print("Nome do Jogador 1: ");
                    String j1 = lerString();
                    System.out.print("Nome do Jogador 2: ");
                    String j2 = lerString();
                    
                    Game jogo = new Game(j1, j2, 3);
                    view.executarJogo(jogo);
                    sala.terminarJogo(jogo);
                    break;
                    
                case 2:
                    // Entrar na sala (fila de espera)
                    System.out.print("Seu nickname: ");
                    String nick = lerString();
                    Game jogoCriado = sala.adicionarJogador(nick);
                    
                    if (jogoCriado != null) {
                        view.executarJogo(jogoCriado);
                        sala.terminarJogo(jogoCriado);
                    } else {
                        view.mostrarMensagem("À espera de oponente... ");
                        // Simulação: adicionar outro jogador
                        System.out.print("Oponente entrou! Nome: ");
                        String oponente = lerString();
                        jogoCriado = sala.adicionarJogador(oponente);
                        if (jogoCriado != null) {
                            view.executarJogo(jogoCriado);
                            sala.terminarJogo(jogoCriado);
                        }
                    }
                    break;
                    
                case 3:
                    view.mostrarEstatisticas(sala);
                    break;
                    
                case 0:
                    continuar = false;
                    view.mostrarMensagem("Obrigado por jogar! Até à próxima.");
                    break;
                    
                default:
                    view.mostrarMensagem("Opção inválida!");
            }
        }
        
        view.fechar();
    }
    
    private static int lerInteiro() {
        return new java.util.Scanner(System.in).nextInt();
    }
    
    private static String lerString() {
        return new java.util.Scanner(System.in).nextLine();
    }
}
