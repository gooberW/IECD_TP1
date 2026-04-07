package server;

import model.GameRoom;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import utils.PlayerDB;
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
            System.out.println("[Handler] Cliente conectado: " + socket.getRemoteSocketAddress());

            while (in.hasNextLine()) {
                String xmlReceived = in.nextLine();

                // valida o xml
                if (!XMLValidator.validate(xmlReceived, "src/data/protocol.xsd")) {
                    sendXML("<protocol><response status='fail' msg='XML invalido face ao XSD'/></protocol>");
                    continue;
                }

                processRequest(xmlReceived);
            }
        } catch (IOException e) {
            System.err.println("[Handler] Erro na ligacao com " + (nickname != null ? nickname : "anonimo") + ": " + e.getMessage());
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

            // O elemento dentro de <protocol>
            Node protocolNode = doc.getDocumentElement();
            Node commandNode = null;
            NodeList children = protocolNode.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    commandNode = children.item(i);
                    break;
                }
            }

            if (commandNode == null) return;
            String command = commandNode.getNodeName();
            Element el = (Element) commandNode;

            switch (command) {
                case "login":
                    handleLogin(el);
                    break;
                case "register":
                    handleRegister(el);
                    break;
                case "play":
                    if (authenticated) {
                        sendXML("<protocol><response status='success' msg='Entraste na fila de espera. A aguardar oponente...'/></protocol>");
                        Server.joinLobby(this);
                    } else {
                        sendXML("<protocol><response status='fail' msg='Precisas de fazer login primeiro!'/></protocol>");
                    }
                    break;
                case "move":
                    handleMove(el);
                    break;
                default:
                    sendXML("<protocol><response status='fail' msg='Comando desconhecido'/></protocol>");
            }
        } catch (Exception e) {
            sendXML("<protocol><response status='fail' msg='Erro no processamento interno'/></protocol>");
        }
    }

    private void handleLogin(Element el) {
        String nick = el.getAttribute("nickname");
        String pass = el.getAttribute("password");

        List<Player> players = PlayerDB.load();
        for (Player p : players) {
            if (p.getNickname().equals(nick) && p.getPassword().equals(pass)) {
                this.nickname = nick;
                this.authenticated = true;
                sendXML("<protocol><response status='success' nickname='"+nick+"' wins='"+p.getTotalWins()+"'/></protocol>");
                return;
            }
        }
        sendXML("<protocol><response status='fail' msg='Credenciais incorretas'/></protocol>");
    }

    private void handleRegister(Element el) {
        String nick = el.getAttribute("nickname");
        List<Player> players = PlayerDB.load();

        // se nick já existe
        for (Player p : players) {
            if (p.getNickname().equalsIgnoreCase(nick)) {
                sendXML("<protocol><response status='fail' msg='Nickname ja existe'/></protocol>");
                return;
            }
        }

        // cria novo jogador (os outros campos vêm do atributo do XSD)
        Player newPlayer = new Player();
        newPlayer.setNickname(nick);
        newPlayer.setPassword(el.getAttribute("password"));
        newPlayer.setAge(Integer.parseInt(el.getAttribute("age")));
        newPlayer.setNationality(el.getAttribute("nationality"));
        newPlayer.setProfilePicture(el.getAttribute("photo"));

        players.add(newPlayer);
        PlayerDB.save(players);
        sendXML("<protocol><response status='success' msg='Registado com sucesso'/></protocol>");
    }

    private void handleMove(Element el) {
        if (!authenticated || currentGame == null) {
            sendXML("<protocol><response status='fail' msg='Nao esta em jogo'/></protocol>");
            return;
        }

        int x1 = Integer.parseInt(el.getAttribute("x1"));
        int y1 = Integer.parseInt(el.getAttribute("y1"));
        int x2 = Integer.parseInt(el.getAttribute("x2"));
        int y2 = Integer.parseInt(el.getAttribute("y2"));

        // a jogada é validada na GameRoom
        currentGame.handleMove(this, x1, y1, x2, y2);
    }

    public void sendXML(String xml) {
        if (out != null) {
            out.println(xml);
            out.flush();
        }
    }

    public String getNickname() { return nickname; }
    public void setGameSession(GameRoom room) { this.currentGame = room; }

    private void cleanup() {
        System.out.println("[Handler] O Cliente " + (nickname != null ? nickname : "") + " desligou-se.");
        try {
            socket.close();
            // lógica para remover o jogador da lista de espera do Server
        } catch (IOException e) { /* ignore */ }
    }
}