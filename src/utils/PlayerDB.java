package utils;

import model.Player;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDB {

    private static final String FILE_PATH = "src/data/players.xml";
    private static final String SCHEMA_PATH = "src/data/players.xsd";

    /**
     * Carrega e valida os jogadores contra o XSD.
     */
    public static List<Player> load() {
        List<Player> players = new ArrayList<>();
        File xmlFile = new File(FILE_PATH);
        File xsdFile = new File(SCHEMA_PATH);

        if (!xmlFile.exists()) return players;

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(xsdFile);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setSchema(schema); // Associa o schema ao factory
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("player");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    Player p = new Player();
                    p.setNickname(getTagValue("nickname", e));
                    p.setPassword(getTagValue("password", e));
                    p.setAge(Integer.parseInt(getTagValue("age", e)));
                    p.setNationality(getTagValue("nationality", e));
                    p.setProfilePicture(getTagValue("photo", e));
                    p.setTotalWins(Integer.parseInt(getTagValue("wins", e)));
                    p.setTotalLosses(Integer.parseInt(getTagValue("losses", e)));
                    p.setAverageTimePerMatch(Long.parseLong(getTagValue("avgTime", e)));
                    players.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("ERRO DE VALIDAÇÃO ou LEITURA: " + e.getMessage());
        }
        return players;
    }

    /**
     * Guarda a lista com Transformer e DOM.
     */
    public static void save(List<Player> players) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("players");
            doc.appendChild(root);

            for (Player p : players) {
                Element player = doc.createElement("player");
                root.appendChild(player);

                player.appendChild(createElement(doc, "nickname", p.getNickname()));
                player.appendChild(createElement(doc, "password", p.getPassword()));
                player.appendChild(createElement(doc, "age", String.valueOf(p.getAge())));
                player.appendChild(createElement(doc, "nationality", p.getNationality()));
                player.appendChild(createElement(doc, "photo", p.getProfilePicture()));
                player.appendChild(createElement(doc, "wins", String.valueOf(p.getTotalWins())));
                player.appendChild(createElement(doc, "losses", String.valueOf(p.getTotalLosses())));
                player.appendChild(createElement(doc, "avgTime", String.valueOf(p.getAverageTimePerMatch())));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);

            try (PrintWriter writer = new PrintWriter(new FileOutputStream(FILE_PATH))) {
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
            }

        } catch (Exception e) {
            System.err.println("Erro ao guardar XML: " + e.getMessage());
        }
    }

    private static String getTagValue(String tag, Element element) {
        Node node = element.getElementsByTagName(tag).item(0);
        return (node != null && node.hasChildNodes()) ? node.getFirstChild().getNodeValue() : "";
    }

    private static Node createElement(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value != null ? value : ""));
        return node;
    }
}