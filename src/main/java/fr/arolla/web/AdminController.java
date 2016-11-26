package fr.arolla.web;

import fr.arolla.core.Players;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Domo-kun on 25/11/2016.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final String password;
    private final Players players;

    public AdminController(@Value("${admin.password:arolli}") String password, Players players) {
        this.password = password;
        this.players = players;
    }

    @RequestMapping(value = "/reset/{passwordPath}/{player}", method = RequestMethod.GET)
    public String resetPlayerScore(@PathVariable String passwordPath, @PathVariable String player) {
        log.debug("reset score requested for player {} with password {}", player, passwordPath);
        if (!password.equals(passwordPath)) {
            return "ERROR - player score not reset";
        }
        players.resetScore(player);
        return "score reset for player " + player;
    }

}
