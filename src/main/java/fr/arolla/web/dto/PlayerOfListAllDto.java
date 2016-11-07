package fr.arolla.web.dto;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerOfListAllDto {

    public final String name;
    public final double cash;
    public final boolean online;

    public PlayerOfListAllDto(String name, double cash, boolean online) {
        this.name = name;
        this.cash = cash;
        this.online = online;
    }
}
