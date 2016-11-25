package fr.arolla.core.model;

import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by fmaury on 24/11/16.
 */
public class LocalDatesTest {

    @Test
    public void should_count_days_between_dates(){

        LocalDate startDate = LocalDate.now();
        LocalDate endDate=startDate.plusDays(5);
        assertThat(LocalDates.nbDaysBefore(startDate, endDate)).isEqualTo(5);
    }

    @Test
    public void should_be_proof_to_null_value(){

        LocalDate today = LocalDate.now();
        assertThat(LocalDates.nbDaysBefore(null, today)).isEqualTo(0);
    }


    @Test
    public void should_be_proof_to_null_return_value(){

        LocalDate today = LocalDate.now();
        assertThat(LocalDates.nbDaysBefore(today, null)).isEqualTo(0);
    }

    @Test
    public void should_count_days_between_dates_when_more_than_month(){
        LocalDate startDate = LocalDate.now();
        LocalDate endDate=startDate.plusMonths(2).plusDays(5);
        assertThat(LocalDates.nbDaysBefore(startDate, endDate)).isEqualTo(66);
    }

}