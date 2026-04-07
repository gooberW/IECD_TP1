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

        if (xml.contains("<response")) {
            String status = extractAttribute(xml, "status");
            String msg = extractAttribute(xml, "msg");
            if (!msg.equals("N/A")) {
                String icon = status.equals("success") ? "[:)] " : "[:(] ERRO: ";
                System.out.println("\n" + icon + msg);
            }
            // guarda o nickname se o login correr bem
            if (status.equals("success") && xml.contains("nickname='")) {
                myNickname = extractAttribute(xml, "nickname");
            }
        }

        if (xml.contains("<match")) {
            String opp = extractAttribute(xml, "opponent");
            System.out.println("\n[D&B] Partida encontrada contra: " + opp);
            occupiedLines.clear();
            conqueredBoxes.clear();
        }

        // atualizacoes do yabuleiro
        if (xml.contains("<update")) {
            // linahs
            String last = extractAttribute(xml, "lastMove");
            if (!last.equals("N/A") && !last.isEmpty()) {
                occupiedLines.add(normalizeKey(last));
            }

            // caixas
            String boxesAttr = extractAttribute(xml, "boxes");
            if (!boxesAttr.equals("N/A") && !boxesAttr.isEmpty()) {
                parseBoxes(boxesAttr);
            }

            drawBoard();

            //turno e pontos
            String next = extractAttribute(xml, "next");
            String scores = extractAttribute(xml, "scores");

            System.out.println("Pontuação: " + scores);
            if (!next.equals("N/A")) {
                System.out.println("Próximo a jogar: " + next);
                if (next.equalsIgnoreCase(myNickname)) {
                    System.out.println("\n>>> É A TUA VEZ! (move x1 y1 x2 y2) <<<");
                }
            }
        }

        // ganme over
        if (xml.contains("<gameOver")) {
            System.out.println("\n=== FIM DO JOGO ===");
            System.out.println(extractAttribute(xml, "msg"));
        }
    }

    private static void parseBoxes(String data) {
        // vem no formato x,y:Letra|x,y:Letra|...
        String[] pairs = data.split("\\|");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                conqueredBoxes.put(parts[0], parts[1]);
            }
        }
    }

    private static String extractAttribute(String xml, String attr) {
        try {
            // procura por nome_do_atributo='
            String search = attr + "='";
            int startPos = xml.indexOf(search);

            if (startPos == -1) return "N/A"; // nao foi encontrado

            startPos += search.length();
            int endPos = xml.indexOf("'", startPos);

            if (endPos == -1) return "N/A";

            return xml.substring(startPos, endPos);
        } catch (Exception e) {
            return "Erro ao extrair o atributo: " + attr;
        }
    }

    private static void showMenu() {
        System.out.println("Comandos disponíveis:");
        System.out.println("  login <nick> <pass>");
        System.out.println("  register <nick> <pass> <age> <nat> <photo>");
        System.out.println("  play");
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