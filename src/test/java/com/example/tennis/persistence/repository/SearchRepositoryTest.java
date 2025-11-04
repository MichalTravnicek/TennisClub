package com.example.tennis.persistence.repository;

import com.example.tennis.persistence.TestTool;
import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.Customer;
import com.example.tennis.persistence.entity.GameType;
import com.example.tennis.persistence.entity.Reservation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

@Import({TennisRepository.class,
        SearchRepository.class})
@DataJpaTest
class SearchRepositoryTest {

    @Autowired
    SearchDAO searchDAO;

    @Autowired
    TennisDAO tennisDAO;

    @Test
    public void testScheduleOverlap(){
        Court court = TestTool.createCourt("TestCourt123", "TestSurface123", 100L);
        var gameTypes = tennisDAO.getAll(GameType.class);
        var gametype = gameTypes.getFirst();
        Reservation reservation = TestTool.createReservation(court, "Pavel","777147452", gametype);
        reservation.setStartTime(LocalDateTime.of(2045, 5, 1, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2045, 5, 10, 0, 0));
        Reservation reservation2 = TestTool.createReservation(court, "Petr","777147888", gametype);
        reservation2.setStartTime(LocalDateTime.of(2045, 5, 12, 0, 0));
        reservation2.setEndTime(LocalDateTime.of(2045, 5, 15, 0, 0));

        tennisDAO.save(reservation);
        tennisDAO.save(reservation2);
        var start = LocalDateTime.of(2045, 5, 9, 0, 0);
        var end = LocalDateTime.of(2045, 5, 10, 0, 0);

        var result = searchDAO.testScheduleOverlap(court.getName(), start, end);
        Assertions.assertThat(result).isTrue();
        var result2 = searchDAO.testScheduleOverlap(court.getName(), start.minusYears(1), end.minusYears(1));
        Assertions.assertThat(result2).isFalse();
        var result3 = searchDAO.testScheduleOverlap(court.getName(), start.plusDays(10), end.plusDays(10));
        Assertions.assertThat(result3).isFalse();
        var result4 = searchDAO.testScheduleOverlap(court.getName(), start.plusDays(2), end.plusDays(2));
        Assertions.assertThat(result4).isFalse();
        var result5 = searchDAO.testScheduleOverlap(court.getName(), start.plusDays(2), end.plusDays(3));
        Assertions.assertThat(result5).isTrue();
        var result6 = searchDAO.testScheduleOverlap(court.getName(), start.plusDays(3), end.plusDays(5));
        Assertions.assertThat(result6).isTrue();
        tennisDAO.delete(reservation2);
        var result7 = searchDAO.testScheduleOverlap(court.getName(), start.plusDays(3), end.plusDays(5));
        Assertions.assertThat(result7).isFalse();
        var result8 = searchDAO.testScheduleOverlap(court.getName(), start.minusDays(8), end);
        Assertions.assertThat(result8).isTrue();
        var result9 = searchDAO.testScheduleOverlap(court.getName(), start.minusDays(8), end, reservation.getId());
        Assertions.assertThat(result9).isFalse();
    }


    @Test
    public void getReservationsForCourt() {
        Court court = TestTool.createCourt("TestCourt123", "TestSurface123", 100L);
        var gameTypes = tennisDAO.getAll(GameType.class);
        var gametype = gameTypes.getFirst();
        Reservation reservation = TestTool.createReservation(court, "Pavel","777147452", gametype);
        reservation.setStartTime(LocalDateTime.of(2045, 5, 1, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2045, 5, 10, 0, 0));
        Reservation reservation2 = TestTool.createReservation(court, "Petr","777147888", gametype);
        reservation2.setStartTime(LocalDateTime.of(2060, 5, 1, 0, 0));
        reservation2.setEndTime(LocalDateTime.of(2060, 5, 10, 0, 0));
        Reservation reservation3 = TestTool.createReservation(court, "Jirka","777147999", gametype);
        reservation3.setStartTime(LocalDateTime.of(2040, 5, 1, 0, 0));
        reservation3.setEndTime(LocalDateTime.of(2040, 5, 10, 0, 0));
        tennisDAO.save(reservation);
        tennisDAO.save(reservation2);
        tennisDAO.save(reservation3);

        var reservations = searchDAO.getReservationsForCourt(court.getName());
        Assertions.assertThat(reservations).isNotNull();
        Assertions.assertThat(reservations.size()).isEqualTo(3);
        Assertions.assertThat(reservations).extracting(Reservation::getCustomer).extracting(Customer::getPhone)
                .containsExactly("777147452", "777147888", "777147999");
    }

    @Test
    public void getReservationsForPhone() {
        Court court = TestTool.createCourt("TestCourt123", "TestSurface123", 100L);
        var gameTypes = tennisDAO.getAll(GameType.class);
        var gametype = gameTypes.getFirst();
        Customer customer = new Customer();
        customer.setName("Filda");
        customer.setPhone("777159357");
        Reservation reservation = TestTool.createReservation(court, customer, gametype);
        reservation.setStartTime(LocalDateTime.of(2045, 5, 1, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2045, 5, 10, 0, 0));
        Reservation reservation2 = TestTool.createReservation(court, customer, gametype);
        reservation2.setStartTime(LocalDateTime.of(2060, 5, 1, 0, 0));
        reservation2.setEndTime(LocalDateTime.of(2060, 5, 10, 0, 0));
        Reservation reservation3 = TestTool.createReservation(court, customer, gametype);
        reservation3.setStartTime(LocalDateTime.of(2000, 5, 1, 0, 0));
        reservation3.setEndTime(LocalDateTime.of(2000, 5, 10, 0, 0));
        tennisDAO.save(reservation);
        tennisDAO.save(reservation2);
        tennisDAO.save(reservation3);

        var futureReservations = searchDAO.getReservationsForPhone("777159357",
                LocalDateTime.of(2025,1,1,1,0,0));
        Assertions.assertThat(futureReservations).isNotNull();
        Assertions.assertThat(futureReservations.size()).isEqualTo(2);
        Assertions.assertThat(futureReservations).extracting(Reservation::getStartTime)
                .isEqualTo(List.of(LocalDateTime.of(2045, 5, 1, 0, 0),
                        LocalDateTime.of(2060, 5, 1, 0, 0)));

        var reservationsAllTime = searchDAO.getReservationsForPhone("777159357",
                null);
        Assertions.assertThat(reservationsAllTime).isNotNull();
        Assertions.assertThat(reservationsAllTime.size()).isEqualTo(3);
        Assertions.assertThat(reservationsAllTime).extracting(Reservation::getStartTime)
                .isEqualTo(List.of(LocalDateTime.of(2000, 5, 1, 0, 0),
                        LocalDateTime.of(2045, 5, 1, 0, 0),
                        LocalDateTime.of(2060, 5, 1, 0, 0)));
    }
}
