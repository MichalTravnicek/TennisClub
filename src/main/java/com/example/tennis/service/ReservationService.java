package com.example.tennis.service;

import com.example.tennis.controller.model.CourtSearch;
import com.example.tennis.controller.model.PhoneSearch;
import com.example.tennis.controller.model.ReservationDTO;

import java.util.List;

public interface ReservationService {

    List<ReservationDTO> getAllReservations();

    List<ReservationDTO> getAllReservationsForCourt(CourtSearch court);

    List<ReservationDTO> getAllReservationsForPhone(PhoneSearch search);

    ReservationDTO getReservation(ReservationDTO reservation);

    ReservationDTO createReservation(ReservationDTO reservation);

    /**
     * Updates only fields that are present in request
     */
    ReservationDTO updateReservation(ReservationDTO reservation);

    void deleteReservation(ReservationDTO reservation);

}
