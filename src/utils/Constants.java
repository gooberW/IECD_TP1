package utils;

public class Constants {
    private Constants() {} // para nao ser instanciada

    public static final class Network {
        public static final String HOST = "localhost";
        public static final int PORT = 5025;
    }

    public static final class Paths {
        public static final String PROTOCOL_XSD = "src/data/protocol.xsd";
        public static final String PLAYERS_XSD  = "src/data/players.xsd";
        public static final String PLAYERS_XML  = "src/data/players.xml";
    }
}
