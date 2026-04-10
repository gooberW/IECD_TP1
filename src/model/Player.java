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
    private String password;
    private int age;
    private String profilePicture; //base64
    private String nationality; // provavelmente vai ser em acrónimo

    // Registo (guardados no XML)
    private int totalWins = 0;
    private int totalLosses = 0;
    private long averageTimePerMatch;


    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() {
        return age;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getNationality() {
        return nationality;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public long getAverageTimePerMatch() {
        return averageTimePerMatch;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    // depois para adicionar o average time deve bastar meter dois metodos
    // na classe do GameRoom
    public void setAverageTimePerMatch(long averageTimePerMatch) {
        this.averageTimePerMatch = averageTimePerMatch;
    }

    // utils

    public int getTotalGamesPlayed() {
        return this.totalWins + this.totalLosses;
    }

    /**
     * Incrementa o registo de vitórias.
     */
    public void addWin() {
        this.totalWins++;
    }

    /**
     * Incrementa o registo de derrotas.
     */
    public void addLoss() {
        this.totalLosses++;
    }

    @Override
    public String toString() {
        return "Player: " + this.nickname + " | " + this.nationality;
    }



}
