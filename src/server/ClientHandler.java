package server;

import model.GameRoom;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import utils.PlayerDB;
import utils.XMLMessageBuilder;
import utils.XMLValidator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private final Socket socket;
    private PrintWriter out;
    private String nickname;
    private GameRoom currentGame;
    private boolean authenticated = false;

    private static final String XSD_PATH = "src/data/protocol.xsd";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                Scanner in = new Scanner(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = out;
            System.out.println("[HANDLER] Cliente conectado: " + socket.getRemoteSocketAddress());

            while (in.hasNextLine()) {
                String xmlReceived = in.nextLine();

                if (!XMLValidator.validate(xmlReceived, XSD_PATH)) {
                    sendErrorResponse("[HANDLER] XML invalido face ao XSD");
                    continue;
                }

                processRequest(xmlReceived);
            }
        } catch (IOException e) {
            System.err.println("[HANDLER] Erro na ligacao com " + (nickname != null ? nickname : "anonimo") + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void processRequest(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            Element protocolRoot = doc.getDocumentElement();
            Node commandNode = getFirstElementChild(protocolRoot);

            if (commandNode == null) return;

            String command = commandNode.getNodeName();
            Element commandElement = (Element) commandNode;

            switch (command) {
                case "login" -> handleLogin(commandElement);
                case "register" -> handleRegister(commandElement);
                case "play" -> handlePlay();
                case "move" -> handleMove(commandElement);
                default -> sendErrorResponse("[ERRO] Comando desconhecido");
            }
        } catch (Exception e) {
            sendErrorResponse("[ERRO] Erro no processamento interno: " + e.getMessage());
        }
    }

    private void handleLogin(Element el) {
        String nick = el.getAttribute("nickname");
        String pass = el.getAttribute("password");
        List<Player> players = PlayerDB.load();

        Document doc = createBaseDocument();
        Element resp = doc.createElement("response");

        Player foundPlayer = players.stream()
                .filter(p -> p.getNickname().equals(nick) && p.getPassword().equals(pass))
                .findFirst()
                .orElse(null);

        if (foundPlayer != null) {
            this.nickname = nick;
            this.authenticated = true;
            resp.setAttribute("status", "success");
            resp.setAttribute("nickname", nick);
            resp.setAttribute("wins", String.valueOf(foundPlayer.getTotalWins()));
            resp.setAttribute("msg", "[D&B] Login efetuado com sucesso");
        } else {
            resp.setAttribute("status", "fail");
            resp.setAttribute("msg", "[ERRO] Credenciais incorretas");
        }

        doc.getDocumentElement().appendChild(resp);
        sendValidatedXML(doc);
    }

    private void handleRegister(Element el) {
        String nick = el.getAttribute("nickname");
        List<Player> players = PlayerDB.load();

        Document doc = createBaseDocument();
        Element resp = doc.createElement("response");

        boolean exists = players.stream().anyMatch(p -> p.getNickname().equalsIgnoreCase(nick));

        if (exists) {
            resp.setAttribute("status", "fail");
            resp.setAttribute("msg", "[ERRO] Nickname ja existe");
        } else {
            Player newPlayer = new Player();
            newPlayer.setNickname(nick);
            newPlayer.setPassword(el.getAttribute("password"));
            newPlayer.setAge(Integer.parseInt(el.getAttribute("age")));
            newPlayer.setNationality(el.getAttribute("nationality"));
            newPlayer.setProfilePicture(el.getAttribute("photo"));

            players.add(newPlayer);
            PlayerDB.save(players);

            resp.setAttribute("status", "success");
            resp.setAttribute("msg", "[D&B] Registado com sucesso");
        }

        doc.getDocumentElement().appendChild(resp);
        sendValidatedXML(doc);
    }

    private void handlePlay() {
        Document doc = createBaseDocument();
        Element resp = doc.createElement("response");

        if (authenticated) {
            resp.setAttribute("status", "success");
            resp.setAttribute("msg", "[D&B] Entraste na fila de espera. A aguardar oponente...");
            Server.joinLobby(this);
        } else {
            resp.setAttribute("status", "fail");
            resp.setAttribute("msg", "[ERRO] Precisas de fazer login primeiro!");
        }

        doc.getDocumentElement().appendChild(resp);
        sendValidatedXML(doc);
    }

    private void handleMove(Element el) {
        if (!authenticated || currentGame == null) {
            sendErrorResponse("[ERRO] Nao esta em jogo ativo");
            return;
        }

        try {
            int x1 = Integer.parseInt(el.getAttribute("x1"));
            int y1 = Integer.parseInt(el.getAttribute("y1"));
            int x2 = Integer.parseInt(el.getAttribute("x2"));
            int y2 = Integer.parseInt(el.getAttribute("y2"));

            currentGame.handleMove(this, x1, y1, x2, y2);
        } catch (NumberFormatException e) {
            sendErrorResponse("[ERRO] Coordenadas invalidas");
        }
    }

    private Document createBaseDocument() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().newDocument();
            Element root = doc.createElement("protocol");
            doc.appendChild(root);
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar Documento DOM");
        }
    }

    protected void sendErrorResponse(String message) {
        Document doc = createBaseDocument();
        Element resp = doc.createElement("response");
        resp.setAttribute("status", "fail");
        resp.setAttribute("msg", message);
        doc.getDocumentElement().appendChild(resp);
        sendValidatedXML(doc);
    }

    public void sendValidatedXML(Document doc) {
        try {
            String xml = XMLMessageBuilder.toString(doc);
            if (XMLValidator.validate(xml, XSD_PATH)) {
                if (out != null) {
                    out.println(xml);
                }
            } else {
                System.err.println("[ERRO] XML de saida invalido: " + xml);
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Falha no envio: " + e.getMessage());
        }
    }

    private Node getFirstElementChild(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) return nl.item(i);
        }
        return null;
    }

    public String getNickname() { return nickname; }
    public void setGameSession(GameRoom room) { this.currentGame = room; }

    private void cleanup() {
        System.out.println("[Handler] Conexão encerrada: " + (nickname != null ? nickname : "Anonimo"));
        Server.removeFromLobby(this);
        try {
            socket.close();
        } catch (IOException e) { /* ignore */ }
    }
}