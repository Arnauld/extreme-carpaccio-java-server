package fr.arolla.core.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Created by fmaury on 24/11/16.
 */
public class LocalDates {

    public static long nbDaysBefore(LocalDate departureDate,LocalDate returnDate){
        if(departureDate==null || returnDate==null){
            return 0;
        }
        return ChronoUnit.DAYS.between(departureDate,returnDate);
    }

}
