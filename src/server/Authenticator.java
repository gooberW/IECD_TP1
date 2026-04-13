package server;

import model.Player;
import utils.PlayerDB;

import java.util.List;

public class Authenticator {

    public static Player login(String nickname, String password) {
        List<Player> players = PlayerDB.load();
        for (Player p : players) {
            if (p.getNickname().equals(nickname) && p.getPassword().equals(password)) {
                return p;
            }
        }
        return null;
    }

    public static boolean register(String nickname, String password, int age,
                                   String nationality, String photo) {
        List<Player> players = PlayerDB.load();
        boolean exists = players.stream().anyMatch(p -> p.getNickname().equals(nickname));
        if (exists) return false;

        Player newPlayer = new Player();
        newPlayer.setNickname(nickname);
        newPlayer.setPassword(password);
        newPlayer.setAge(age);
        newPlayer.setNationality(nationality);
        newPlayer.setProfilePicture(photo);

        players.add(newPlayer);
        PlayerDB.save(players);
        return true;
    }

    public static boolean changePhoto(Player player, String newPhoto) {
        if (newPhoto == null || newPhoto.trim().isEmpty()) return false;

        List<Player> players = PlayerDB.load();
        for (Player p : players) {
            if (p.getNickname().equals(player.getNickname())) {
                p.setProfilePicture(newPhoto);
                player.setProfilePicture(newPhoto);
                break;
            }
        }
        PlayerDB.save(players);
        return true;
    }
}