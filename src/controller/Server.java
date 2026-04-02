package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

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

    public static void main(String[] args) {

        // try-with-resources: garante que o ServerSocket seja fechado automaticamente.
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("[Servidor] Servidor iniciado no porto: " + DEFAULT_PORT);

            while (true) {
                System.out.println("[Servidor] Waiting...");

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

    }

}
