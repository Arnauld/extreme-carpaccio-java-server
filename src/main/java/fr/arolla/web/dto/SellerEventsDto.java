package fr.arolla.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arolla.core.Event;

import java.util.List;

/**
 * Created by fmaury on 13/11/16.
 */
public class SellerEventsDto  {

    @JsonProperty("count")
    private Integer nbEvents;
    @JsonProperty("fromTick")
    private Integer fromTick;
    @JsonProperty("events")
    private List<Event> events;


    public SellerEventsDto(int nbEvents, int fromTick, List<Event> events) {
        this.nbEvents = nbEvents;
        this.fromTick = fromTick;
        this.events = events;
    }

}
