package fr.arolla.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerRegistrationDto {
    @JsonProperty("name")
    public String username;

    @JsonProperty("password")
    public String password;

    @JsonProperty("url")
    public String url;

    // ugly thing for application/form-url-encoded
    public PlayerRegistrationDto() {
    }

    // ugly thing for application/form-url-encoded
    public void setName(String username) {
        this.username = username;
    }

    // ugly thing for application/form-url-encoded
    public void setUrl(String url) {
        this.url = url;
    }

    // ugly thing for application/form-url-encoded
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonCreator
    public PlayerRegistrationDto(@JsonProperty("name") String username,
                                 @JsonProperty("password") String password,
                                 @JsonProperty("url") String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }
}
