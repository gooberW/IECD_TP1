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

    // guarda as linhas ocupadas: "0,0-0,1"
    private static java.util.Set<String> occupiedLines = new java.util.HashSet<>();
    // guarda o dono da caixa: "0,0" -> "A"
    private static java.util.Map<String, String> conqueredBoxes = new java.util.HashMap<>();
    private static int currentGridSize = 3; // Podes receber isto no <match>

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
        } else if (xml.contains("<gameOver")) {
            System.out.println("\n=== FIM DE JOGO ===");
            System.out.println(extractAttribute(xml, "msg"));
        } else if (xml.contains("<update")) {
            String lastMove = extractAttribute(xml, "lastMove");
            if (!lastMove.isEmpty() && !lastMove.equals("N/A")) {
                // Normaliza a chave (menor ponto primeiro) para garantir o match
                occupiedLines.add(normalizeKey(lastMove));
            }

            drawBoard();

            String next = extractAttribute(xml, "next");
            System.out.println("Pontuação: " + extractAttribute(xml, "scores"));
            System.out.println("Próximo: " + next);
            if (next.equals(myNickname)) System.out.println(">>> É A TUA VEZ! <<<");
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

    private static void drawBoard() {
        System.out.println("\n--- TABULEIRO (Dots & Boxes) ---");

        //imprime os números das colunas no topo
        System.out.print("  ");
        for (int j = 0; j < currentGridSize; j++) System.out.print(j + "   ");
        System.out.println();

        for (int i = 0; i < currentGridSize; i++) {
            System.out.print(i + " "); // Número da linha lateral
            for (int j = 0; j < currentGridSize; j++) {
                System.out.print("."); // O Ponto
                if (j < currentGridSize - 1) {
                    String key = getLineKey(i, j, i, j + 1);
                    System.out.print(occupiedLines.contains(key) ? "---" : "   ");
                }
            }
            System.out.println();

            if (i < currentGridSize - 1) {
                System.out.print("  ");
                for (int j = 0; j < currentGridSize; j++) {
                    String vKey = getLineKey(i, j, i + 1, j);
                    System.out.print(occupiedLines.contains(vKey) ? "|" : " ");

                    // desenha a inicial do dono da caixa no meio
                    if (j < currentGridSize - 1) {
                        String boxKey = i + "," + j;
                        String owner = conqueredBoxes.getOrDefault(boxKey, " ");
                        System.out.print(" " + owner + " ");
                    }
                }
                System.out.println();
            }
        }
    }

    private static String getLineKey(int x1, int y1, int x2, int y2) {
        if (x1 < x2 || (x1 == x2 && y1 < y2)) return x1 + "," + y1 + "-" + x2 + "," + y2;
        return x2 + "," + y2 + "-" + x1 + "," + y1;
    }

    public static String normalizeKey(String key) {
        try {
            String[] points = key.split("-");
            String[] p1 = points[0].split(",");
            String[] p2 = points[1].split(",");

            int x1 = Integer.parseInt(p1[0]);
            int y1 = Integer.parseInt(p1[1]);
            int x2 = Integer.parseInt(p2[0]);
            int y2 = Integer.parseInt(p2[1]);

            if (x1 < x2 || (x1 == x2 && y1 < y2)) {
                return x1 + "," + y1 + "-" + x2 + "," + y2;
            } else {
                return x2 + "," + y2 + "-" + x1 + "," + y1;
            }
        } catch (Exception e) {
            return key;
        }
    }
}