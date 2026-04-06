package client;

import java.io.IOException;
import java.net.*;

public class Client {
    /**
     * Representa o cliente (TCP)
     * ---
     * Pode ser FAT ou THIN (ou uma mistura)
     * - FAT - Contém lógica de negócio
     * - THIN - Pouco código, chama maior parte ao servidor (lógica de negócio centralizada no servidor)
     * - Acho melhor usar THIN
     */

    public final static String DEFAULT_HOST = "localhost";
    public final static int DEFAULT_PORT = 5025;

    public static void main(String[] args) {

        // Uso de try-with-resources para garantir que o socket fecha automaticamente
        try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT)) {


        } catch (IOException e) {
            System.err.println("[!] Erro na ligação: " + e.getMessage());
        }
        System.out.println("Cliente finalizado.");
    }
}
