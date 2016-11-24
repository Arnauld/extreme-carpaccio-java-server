package fr.arolla.core.model;

import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by fmaury on 24/11/16.
 */
public class TravelDateTest {

    @Test
    public void should_count_days_between_dates(){

        LocalDate today = LocalDate.now();
        TravelDate startDate=new TravelDate(today);
        TravelDate endDate=new TravelDate(today.plusDays(5));
        assertThat(startDate.nbDaysBefore(endDate)).isEqualTo(5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_accept_null_value_on_TravelDate(){
        new TravelDate(null);
    }

    @Test
    public void should_be_proof_to_null_value(){

        LocalDate today = LocalDate.now();
        TravelDate startDate=new TravelDate(today);
        assertThat(startDate.nbDaysBefore(null)).isEqualTo(0);
    }

    @Test
    public void should_count_days_between_dates_when_more_than_month(){

        LocalDate today = LocalDate.now();
        TravelDate startDate=new TravelDate(today);
        TravelDate endDate=new TravelDate(today.plusMonths(2).plusDays(5));
        assertThat(startDate.nbDaysBefore(endDate)).isEqualTo(66);
    }

}