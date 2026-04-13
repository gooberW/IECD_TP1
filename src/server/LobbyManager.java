package server;

import java.util.HashMap;
import java.util.Map;
import controller.GameRoom;

public class LobbyManager {
    // mapeia o tamanho da board (int) para o jogador
    private static final Map<Integer, ClientHandler> waiting = new HashMap<>();

    public static synchronized void join(ClientHandler handler, int size) {
        ClientHandler opponent = waiting.get(size);

        if (opponent != null && opponent != handler) {
            waiting.remove(size);
            new GameRoom(opponent, handler, size);
        } else {
            waiting.put(size, handler);
        }
    }

    public static synchronized void remove(ClientHandler handler) {
        waiting.values().remove(handler);
    }
}
