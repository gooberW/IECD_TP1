package server;

import model.GameRoom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import utils.XMLValidator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Server {
    /**
     * Representa o servidor (TCP)
     *
     * Se usarmos cliente THIN, manter a business logic centralizada aqui
     *
     * Decidir se usar concorrente vs iterativo
     *
     * Vai ter as regras do jogo, vai guardar as jogadas, etc (?)
     * Talvez vá buscar estas coisas a classe Game / GameRoom (?)
     */

    public final static int DEFAULT_PORT = 5025;

    private static final List<ClientHandler> lobby = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {

        // try-with-resources: garante que o ServerSocket seja fechado automaticamente.
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("[Servidor] Servidor iniciado no porto: " + DEFAULT_PORT);

            while (true) {
                System.out.println("[Servidor] À espera...");

                // aceita a ligação do cliente (Bloqueado até que alguém ligue)
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Servidor] Cliente ligado: " + clientSocket.getRemoteSocketAddress());

                /*
                 * Em vez de criar uma classe 'Thread' ou 'Runnable' externa,
                 * usa uma Expressão Lambda () -> { ... } para definir a tarefa.
                 * Thread.ofVirtual() cria threads ultra-leves que não pesam na RAM.
                 */
                Thread.ofVirtual().start(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[Servidor] Erro: " + e.getLocalizedMessage());
        }
    }

    /**
     * servidor dedicado: contém toda a lógica de atendimento do cliente.
     */
    private static void handleClient(Socket connection) {
        ClientHandler handler = new ClientHandler(connection);
        handler.start();
    }

    public static void joinLobby(ClientHandler player) {
        synchronized (lobby) {
            if (lobby.contains(player)) {
                player.sendXML("<protocol><response status='fail' msg='Ja estas na fila!'/></protocol>");
                return;
            }

            lobby.add(player);

            if (lobby.size() >= 2) {
                ClientHandler p1 = lobby.remove(0);
                ClientHandler p2 = lobby.remove(0);
                new GameRoom(p1, p2, 5); // Inicia a sessão
            }
        }
    }

}
