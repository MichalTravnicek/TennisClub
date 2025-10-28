package com.example.tennis.persistence;

import com.example.tennis.persistence.entity.*;

import java.time.LocalDateTime;

public class TestTool {

    /**
     * Generate Reservation without GAME TYPE
     * @param court
     * @return
     */

    public static Reservation createReservation(Court court, String name, String phone){
        Reservation reservation = new Reservation();
        reservation.setCourt(court);
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        reservation.setCustomer(customer);
        var start = LocalDateTime.of(2045, 5, 1, 0, 0);
        var end = LocalDateTime.of(2045, 5, 2, 0, 0);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        return reservation;
    }

    public static Court createCourt(String name, String surfaceName, Long surfaceMultiply){
        Surface surface = new Surface();
        surface.setName(surfaceName);
        surface.setPricePerMinute(surfaceMultiply);
        Court court = new Court();
        court.setName(name);
        court.setSurface(surface);
        return court;
    }

}
