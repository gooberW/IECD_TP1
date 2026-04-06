package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public final static String DEFAULT_HOST = "localhost";
    public final static int DEFAULT_PORT = 5025;

    private static PrintWriter out;
    private static boolean running = true;
    private static String myNickname = "";

    public static void main(String[] args) {
        try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT)) {
            out = new PrintWriter(socket.getOutputStream(), true);
            Scanner in = new Scanner(socket.getInputStream());
            Scanner keyboard = new Scanner(System.in);

            System.out.println("=== PONTOS E CAIXAS (Dots & Boxes) ===");

            Thread listener = new Thread(() -> {
                while (in.hasNextLine()) {
                    String response = in.nextLine();
                    processServerMessage(response);
                }
                System.out.println("\n[CLIENT] Ligação perdida com o servidor.");
                running = false;
            });
            listener.setDaemon(true);
            listener.start();

            showMenu();
            while (running) {
                System.out.print("> ");
                String input = keyboard.nextLine().trim();
                if (input.equalsIgnoreCase("sair")) break;

                String xml = parseInputToXML(input);
                if (xml != null) {
                    out.println(xml);
                } else {
                    System.out.println("[CLIENT] Comando inválido. Use: login, register ou move.");
                }
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Erro na ligação: " + e.getMessage());
        }
    }

    /**
     * Transforma comandos simples de consola no XML do protocolo.
     */
    private static String parseInputToXML(String input) {
        String[] parts = input.split(" ");
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "login": // login nick pass
                    return String.format("<protocol><login nickname='%s' password='%s'/></protocol>", parts[1], parts[2]);

                case "register": // register nick pass age nat photo
                    return String.format("<protocol><register nickname='%s' password='%s' age='%s' nationality='%s' photo='%s'/></protocol>",
                            parts[1], parts[2], parts[3], parts[4], parts[5]);

                case "play":
                    return "<protocol><play></play></protocol>";

                case "move": // move x1 y1 x2 y2
                    return String.format("<protocol><move x1='%s' y1='%s' x2='%s' y2='%s'/></protocol>",
                            parts[1], parts[2], parts[3], parts[4]);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Processa o XML vindo do servidor e mostra na consola.
     */
    private static void processServerMessage(String xml) {
        // TODO: apresentar o tabuleiro

        if (xml.contains("<response status='success'")) {
            System.out.println("\n[OK] Operação realizada com sucesso.");
        } else if (xml.contains("<response status='fail'")) {
            System.out.println("\n[ERRO] " + extractAttribute(xml, "msg"));
        } else if (xml.contains("<match")) {
            System.out.println("\n[JOGO] Partida encontrada contra: " + extractAttribute(xml, "opponent"));
        } else if (xml.contains("<update")) {
            String next = extractAttribute(xml, "next");
            String scores = extractAttribute(xml, "scores");
            String last = extractAttribute(xml, "lastMove");

            System.out.println("\n--- TABULEIRO ATUALIZADO ---");
            System.out.println("Última jogada: " + last);
            System.out.println("Pontuação: " + scores);
            System.out.println("Próximo a jogar: " + next);
            if (next.equals(myNickname)) System.out.println(">>> É A TUA VEZ! <<<");
        } else if (xml.contains("<gameOver")) {
            System.out.println("\n=== FIM DE JOGO ===");
            System.out.println(extractAttribute(xml, "msg"));
        }
    }

    private static String extractAttribute(String xml, String attr) {
        try {
            String search = attr + "='";
            int start = xml.indexOf(search) + search.length();
            int end = xml.indexOf("'", start);
            return xml.substring(start, end);
        } catch (Exception e) { return "N/A"; }
    }

    private static void showMenu() {
        System.out.println("Comandos disponíveis:");
        System.out.println("  login <nick> <pass>");
        System.out.println("  register <nick> <pass> <age> <nat> <photo>");
        System.out.println("  move <x1> <y1> <x2> <y2>");
        System.out.println("  sair");
    }
}