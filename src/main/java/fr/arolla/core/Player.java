package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Player {
    private final String username;
    private final String password;
    private double cash;
    private boolean online;
    private String url;

    public Player(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public String username() {
        return username;
    }

    public double cash() {
        return cash;
    }

    public void cash(double cash) {
        this.cash = cash;
    }

    public String url() {
        return url;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void changeUrl(String url) {
        this.url = url;
    }

    public String password() {
        return password;
    }

}
