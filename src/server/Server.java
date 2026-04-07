package server;

import model.GameRoom;
import java.io.IOException;
import java.net.*;
import java.util.*;

//servidor concorrente
public class Server {
    public final static int DEFAULT_PORT = 5025;
    // usa-se ynchronizedList porque o ArrayList não é thread-safe.
    // como várias threads (ClientHandlers) podem tentar adicionar ou remover jogadores
    // ao mesmo tempo, a sincronização evita a corrupção da memória e exceções de modificação.
    private static final List<ClientHandler> lobby = Collections.synchronizedList(new ArrayList<>());

    // ssa-se synchronizedMap pela mesma razão, mas desta vez para não se mexer nos lobbies em simultaneo.
    // o mapa fica | tamanho tabuleiro (int) - [JogadorA, JogadorB, ...] |
    private static final Map<Integer, List<ClientHandler>> lobbies = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("[SERVIDOR] Servidor a correr no porto: " + DEFAULT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVIDOR] Conexão aceite: " + clientSocket.getRemoteSocketAddress());

                //threads virtuais sao menos pesadas que threads normais
                Thread.ofVirtual().start(() -> {
                    ClientHandler handler = new ClientHandler(clientSocket);
                    handler.run();
                });
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Erro crítico: " + e.getMessage());
        }
    }

    public static void joinLobby(ClientHandler player, int size) {
        synchronized (lobbies) { // monitor para que nao hajam race conditions
            List<ClientHandler> queue = lobbies.computeIfAbsent(size, k -> new ArrayList<>());

            if (queue.contains(player)) {
                player.sendErrorResponse("Já estás na fila para " + size + "x" + size);
                return;
            }

            queue.add(player);
            System.out.println("[LOBBY] Nick: " + player.getNickname() + " | Tamanho: " + size + " | Em espera: " + queue.size());

            if (queue.size() >= 2) {
                ClientHandler p1 = queue.remove(0);
                ClientHandler p2 = queue.remove(0);
                new GameRoom(p1, p2, size);
            }
        }
    }

    public static void removeFromLobby(ClientHandler player) {
        synchronized (lobby) { // monitor para que nao hajam race conditions
            lobby.remove(player);
        }
    }

}