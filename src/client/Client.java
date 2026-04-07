package client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.XMLMessageBuilder;
import utils.XMLValidator;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * O cliente funciona como uma interface de visualização e recolha de dados, com uma
 * dependência direta das decisões do servidor (THIN client). A sua principal tarefa
 * consiste na renderização gráfica do tabuleiro em ambiente de consola e na conversão
 * dos comandos inseridos pelo utilizador para o formato XML definido no protocolo.
 */
public class Client {
    public final static String DEFAULT_HOST = "localhost";
    public final static int DEFAULT_PORT = 5025;

    private static PrintWriter out; // para onde escreve as mensagens XML
    private static boolean running = true;
    private static String myNickname = "";

    // usa-se um HashSet para armazenar as linhas traçadas porque
    // esta estrutura não permite duplicados e é muito rapida
    private static Set<String> occupiedLines = new HashSet<>();

    private static Map<String, String> conqueredBoxes = new HashMap<>();
    private static int currentGridSize = 3;

    public static void main(String[] args) {
        try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT)) {
            out = new PrintWriter(socket.getOutputStream(), true);
            Scanner in = new Scanner(socket.getInputStream());
            Scanner keyboard = new Scanner(System.in);

            System.out.println("=== Dots & Boxes ===");

            // cria se uma thread para ler do socket
            Thread listener = new Thread(() -> {
                while (in.hasNextLine()) {
                    String response = in.nextLine();
                    processServerMessage(response);

                }
                System.out.println("\n[CLIENT] Ligação perdida com o servidor.");
                running = false;
            });
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
                    System.out.println("[CLIENT] Comando inválido ou erro ao gerar XML.");
                }
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Erro: " + e.getMessage());
        }
    }

    /**
     * Converte o input de texto do terminal (ex: "move 0 0 0 1") num documento XML
     * válido de acordo com o XSD.
     */
    private static String parseInputToXML(String input) {
        try {
            String[] parts = input.split(" ");
            String cmd = parts[0].toLowerCase();

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("protocol");
            doc.appendChild(root);
            Element action = doc.createElement(cmd);

            switch (cmd) {
                case "login":
                    action.setAttribute("nickname", parts[1]);
                    action.setAttribute("password", parts[2]);
                    break;
                case "register":
                    action.setAttribute("nickname", parts[1]);
                    action.setAttribute("password", parts[2]);
                    action.setAttribute("age", parts[3]);
                    action.setAttribute("nationality", parts[4]);
                    action.setAttribute("photo", parts[5]);
                    break;
                case "play":
                    // se o utilizador só escrever "play", assume 3 por defeito (?)
                    String size = (parts.length > 1) ? parts[1] : "3";
                    action.setAttribute("size", size);
                    break;
                case "move":
                    action.setAttribute("x1", parts[1]);
                    action.setAttribute("y1", parts[2]);
                    action.setAttribute("x2", parts[3]);
                    action.setAttribute("y2", parts[4]);
                    break;
                default: return null;
            }

            root.appendChild(action);
            return XMLMessageBuilder.toString(doc);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Processa as mensagens de entrada
     */
    private static void processServerMessage(String xml) {
        try {
            // string -> doc
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes()));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            Node commandNode = getFirstElementChild(root);
            if (commandNode == null) return;

            String tagName = commandNode.getNodeName();
            Element el = (Element) commandNode;

            if (tagName.equals("response")) {
                String status = el.getAttribute("status");
                String msg = el.getAttribute("msg");
                System.out.println(msg);

                if (status.equals("success") && el.hasAttribute("nickname")) {
                    myNickname = el.getAttribute("nickname");
                }
            }
            else if (tagName.equals("match")) {
                System.out.println("\n[D&B] Partida contra: " + el.getAttribute("opponent"));
                occupiedLines.clear();
                conqueredBoxes.clear();
                currentGridSize = Integer.parseInt(el.getAttribute("size"));
            }
            else if (tagName.equals("update")) {
                String last = el.getAttribute("lastMove");
                if (!last.isEmpty()) occupiedLines.add(normalizeKey(last));

                String boxes = el.getAttribute("boxes");
                if (!boxes.isEmpty()) parseBoxes(boxes);

                drawBoard();
                System.out.println("Pontuação: " + el.getAttribute("scores"));
                System.out.println("Próximo: " + el.getAttribute("next"));
                if (el.getAttribute("next").equalsIgnoreCase(myNickname)) {
                    System.out.println(">>> É A TUA VEZ! <<<");
                    System.out.println("> move <x1> <y1> <x2> <y2>");
                }
            }
            else if (tagName.equals("gameOver")) {
                System.out.println("\n=== FIM DO JOGO ===\n" + el.getAttribute("msg"));
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem do servidor.");
        }
        System.out.flush(); // é preciso para aparecerem mensagens no powersehll / cmd
    }

    /**
     * Retorna o primeiro elemento filho de um determinado nó.
     * @param parent - Nó "pai"
     * @return Primeiro elemento filho
     */
    private static Node getFirstElementChild(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) return nl.item(i);
        }
        return null;
    }

    /**
     * Transforma a string de dados das caixas (ex: "0,0:g|0,1:o") em entradas no mapa
     * de caixas conquistadas. Utiliza delimitadores específicos ("|") para separar
     * coordenadas de identificadores de jogadores.
     */
    private static void parseBoxes(String data) {
        String[] pairs = data.split("\\|");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) conqueredBoxes.put(parts[0], parts[1]);
        }
    }

    private static void showMenu() {
        System.out.println("\nComandos: \nlogin <nickname> <password>" +
                "\nregister <nickname> <password> <age> <nat> <photo> \nplay <tamanho> (ex: play 3 para 3x3 pontos, play 5 para 5x5)\"); \nsair");
    }

    private static void drawBoard() {
        System.out.println("\n--- TABULEIRO ---");
        System.out.print("  ");
        for (int j = 0; j < currentGridSize; j++) System.out.print(j + "   ");
        System.out.println();
        for (int i = 0; i < currentGridSize; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < currentGridSize; j++) {
                System.out.print(".");
                if (j < currentGridSize - 1) {
                    System.out.print(occupiedLines.contains(getLineKey(i, j, i, j + 1)) ? "---" : "   ");
                }
            }
            System.out.println();
            if (i < currentGridSize - 1) {
                System.out.print("  ");
                for (int j = 0; j < currentGridSize; j++) {
                    System.out.print(occupiedLines.contains(getLineKey(i, j, i + 1, j)) ? "|" : " ");
                    if (j < currentGridSize - 1) {
                        System.out.print(" " + conqueredBoxes.getOrDefault(i + "," + j, " ") + " ");
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * Cria uma chave única e padronizada para representar uma linha entre dois pontos.
     * Ordena os pontos de forma a que (0,0)-(0,1) e (0,1)-(0,0) resultem na mesma chave.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return chave padronizada
     */
    private static String getLineKey(int x1, int y1, int x2, int y2) {
        if (x1 < x2 || (x1 == x2 && y1 < y2)) return x1 + "," + y1 + "-" + x2 + "," + y2;
        return x2 + "," + y2 + "-" + x1 + "," + y1;
    }

    /**
     * Normaliza os pontos para que o ponto "menor" fique sempre à esquerda do "maior"
     * @param key
     * @return
     */
    public static String normalizeKey(String key) {
        try {
            String[] points = key.split("-");
            return getLineKey(
                    Integer.parseInt(points[0].split(",")[0]), Integer.parseInt(points[0].split(",")[1]),
                    Integer.parseInt(points[1].split(",")[0]), Integer.parseInt(points[1].split(",")[1])
            );
        } catch (Exception e) { return key; }
    }
}