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

/**
 * Gere a comunicação individual com um cliente. Esta classe processa o ciclo de vida
 * do jogador, desde o login até ao fim da ligação, funciona como um
 * Skeleton (ref: jogo do galo do professor).
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private PrintWriter out; // para onde vai escrever o XML
    private String nickname;
    private GameRoom currentGame;
    private boolean authenticated = false; // ta logged in?

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
                // recebe o XML e verifica se é valido
                if (!XMLValidator.validate(xmlReceived, XSD_PATH)) {
                    sendErrorResponse("[HANDLER] XML invalido face ao XSD");
                    continue;
                }

                processRequest(xmlReceived);
            }
        } catch (IOException e) {
            System.err.println("[HANDLER] Erro na ligacao com " +
                    (nickname != null ? nickname : "anonimo") + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Recebe o XML enviado do cliente, e processa-o consoante o comando.
     * @param xml
     */
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
                case "play" -> handlePlay(commandElement);
                case "move" -> handleMove(commandElement);
                default -> sendErrorResponse("[ERRO] Comando desconhecido");
            }
        } catch (Exception e) {
            sendErrorResponse("[ERRO] Erro no processamento interno: " + e.getMessage());
        }
    }

    /**
     * Vai buscar os elementos precisos para o login
     * @param el
     */
    private void handleLogin(Element el) {
        String nick = el.getAttribute("nickname");
        String pass = el.getAttribute("password");
        List<Player> players = PlayerDB.load();

        Document doc = createBaseDocument();
        Element resp = doc.createElement("response");

        // procura o jogador na lista
        Player foundPlayer = findPlayer(players, nick, pass);

        // se encontrar, mete os atributos na resposta a enviar para o cliente
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

        boolean exists = (findPlayer(players, nick) != null);

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

    private void handlePlay(Element el) {
        if (authenticated) {
            int size = 3; // default
            if (el.hasAttribute("size")) {
                size = Integer.parseInt(el.getAttribute("size"));
            }

            if(size < 2) {
                sendErrorResponse("Tamanho inválido. O tamanho mínimo para um tabuleiro é 2x2.");
                return;
            }

            if(size > 10) {
                sendErrorResponse("Tamanho inválido. O tamanho máximo para um tabuleiro é 10x10.");
                return;
            }

            Document doc = createBaseDocument();
            Element resp = doc.createElement("response");
            resp.setAttribute("status", "success");
            resp.setAttribute("msg", "Na fila para tabuleiro " + size + "x" + size);
            doc.getDocumentElement().appendChild(resp);
            sendValidatedXML(doc);

            Server.joinLobby(this, size);
        } else {
            sendErrorResponse("Faz login primeiro!");
        }
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

    private Player findPlayer(List<Player> players, String nickname) {
        return findPlayer(players, nickname, null);
    }

    private Player findPlayer(List<Player> players, String nickname, String password) {
        for (Player player : players) {
            if (player.getNickname().equals(nickname) &&
                    (password == null || player.getPassword().equals(password))) {
                return player;
            }
        }
        return null;
    }

    public String getNickname() { return nickname; }
    public void setGameSession(GameRoom room) { this.currentGame = room; }

    private void cleanup() {
        System.out.println("[HANDLER] Conexão encerrada: " + (nickname != null ? nickname : "Anonimo"));
        Server.removeFromLobby(this);
        try {
            socket.close();
        } catch (IOException e) { /* ignore */ }
    }
}