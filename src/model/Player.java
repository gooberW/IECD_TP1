package model;

public class Player {
    /**
     * Classe que representa um jogador
     *
     *
     * O player deve guardar a quantidade de jogadas, a quandidade de caixas que é dono.(?)
     * Cada player guarda a sua cor?
     *
     * Um jogador faz o seu auto registo indicando uma alcunha (nickname), uma senha (password) e uma fotografia
     * que pode alterar posteriormente. Sobre cada jogador é necessário conhecer a nacionalidade, a idade, o registo de
     * vitórias, de derrotas e do tempo gasto em cada jogo.
     */
    private String nickname;
    private int age;
    private String profilePicture; //base64
    private String nationality; // provavelmente vai ser em acrónimo

    // Registo
    private int totalWins = 0;
    private int totalLosses = 0;
    private long averageTimePerMatch;


    public String toString() {
        return "Player: " + this.nickname + " | " + this.nationality;
    }

}
