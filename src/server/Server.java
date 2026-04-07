package server;

import model.GameRoom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//servidor concorrente
public class Server {
    public final static int DEFAULT_PORT = 5025;
    private static final List<ClientHandler> lobby = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("[SERVIDOR] Servidor a correr no porto: " + DEFAULT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVIDOR] Conexão aceite: " + clientSocket.getRemoteSocketAddress());

                Thread.ofVirtual().start(() -> {
                    ClientHandler handler = new ClientHandler(clientSocket);
                    handler.run();
                });
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Erro crítico: " + e.getMessage());
        }
    }

    public static void joinLobby(ClientHandler player) {
        synchronized (lobby) {
            if (lobby.contains(player)) {
                player.sendErrorResponse("Já estás na fila de espera!");
                return;
            }

            lobby.add(player);
            System.out.println("[LOBBY] " + player.getNickname() + " entrou na fila.");

            if (lobby.size() >= 2) {
                ClientHandler p1 = lobby.remove(0);
                ClientHandler p2 = lobby.remove(0);

                new GameRoom(p1, p2, 5);
            }
        }
    }

    public static void removeFromLobby(ClientHandler player) {
        synchronized (lobby) {
            lobby.remove(player);
        }
    }

}