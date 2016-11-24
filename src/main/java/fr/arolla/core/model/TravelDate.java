package fr.arolla.core.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Created by fmaury on 24/11/16.
 */
public class TravelDate {

    private LocalDate value;


    public TravelDate(LocalDate value){
        if(value==null){
            throw new IllegalArgumentException("TravelDate need value on initialization");
        }
        this.value=value;
    }

    public long nbDaysBefore(TravelDate date){
        if(date==null){
            return 0;
        }
        return ChronoUnit.DAYS.between(value,date.value);
    }

}
