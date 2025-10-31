package com.example.tennis;

import com.example.tennis.controller.model.CourtDTO;
import com.example.tennis.controller.model.ReservationDTO;
import com.example.tennis.persistence.entity.*;
import com.example.tennis.persistence.repository.SearchRepository;
import com.example.tennis.persistence.repository.TennisRepository;
import com.example.tennis.service.CourtService;
import com.example.tennis.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty("tennis.init.data")
@RequiredArgsConstructor
public class InitRunner implements CommandLineRunner {

    private final TennisRepository repository;
    private final SearchRepository searchRepository;
    private final CourtService courtService;
    private final ReservationService reservationService;

    @Override
    public void run(String... args) throws Exception {
        System.err.println("STARTING INIT RUNNER");
        Surface surface = new Surface("Dirt", 100L);
        Surface surface2 = new Surface("Gravel", 150L);
        repository.save(surface);
        repository.save(surface2);
        CourtDTO court = new CourtDTO(UUID.fromString("4930d289-835b-4f60-a326-9bd981389ae6"),"Court 1","Dirt");
        CourtDTO court2 = new CourtDTO(UUID.fromString("d6385040-1679-4630-9493-5f15a0373fb9"),"Court 2","Dirt");
        CourtDTO court3 = new CourtDTO(UUID.fromString("b9376343-e083-4aa7-8af6-1d22693646de"),"Court 3","Gravel");
        CourtDTO court4 = new CourtDTO(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),"Court 4","Gravel");

        courtService.createCourt(court);
        courtService.createCourt(court2);
        courtService.createCourt(court3);
        courtService.createCourt(court4);

        System.err.println(repository.getAll(GameType.class));

        Customer customer = new Customer("Emil Doktor","777123456");
        Customer customer2 = new Customer("Pavel Prochazka","777321987");
        repository.save(customer);
        repository.save(customer2);
        System.err.println(customer);

        var nextYear = LocalDateTime.now().plusYears(1).getYear();
        var reservation = new ReservationDTO(UUID.fromString("2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"),"Court 1",
                "Singles", null, null,
                "777123456", null, null);
        reservation.setStartTime(LocalDateTime.of(nextYear, 5, 1, 0, 0));
        reservation.setEndTime(LocalDateTime.of(nextYear, 5, 2, 0, 0));

        var reservation2 = new ReservationDTO(UUID.fromString("54027dc9-a61d-4ce1-8616-7397595d00e3"),"Court 1",
                "Doubles", null, null,
                "777123456", null, null);
        reservation2.setStartTime(LocalDateTime.of(nextYear, 6, 10, 0, 0));
        reservation2.setEndTime(LocalDateTime.of(nextYear, 6, 15, 0, 0));

        var reservation3 = new ReservationDTO(UUID.fromString("b423ccb2-0fb7-4df4-b510-55c89f682214"),"Court 1",
                "Singles", null, null,
                "777321987", null, null);
        reservation3.setStartTime(LocalDateTime.of(nextYear, 7, 12, 0, 0));
        reservation3.setEndTime(LocalDateTime.of(nextYear, 7, 14, 0, 0));

        reservationService.createReservation(reservation);
        reservationService.createReservation(reservation2);
        reservationService.createReservation(reservation3);

        var reservations = searchRepository.getReservationsForCourt("Court 1");
        System.err.println("For court1: "+ reservations);

        var reservations2 = searchRepository.getReservationsForPhone("777123456",null);
        System.err.println("For phone: "+ reservations2);
    }
}
