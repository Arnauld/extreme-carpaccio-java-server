package fr.arolla.web.dto;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ErrorDto {
    public final String code;
    public final String message;

    public ErrorDto(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
