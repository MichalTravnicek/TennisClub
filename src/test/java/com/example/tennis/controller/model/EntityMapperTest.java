package com.example.tennis.controller.model;

import com.example.tennis.persistence.TestTool;
import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.GameType;
import com.example.tennis.persistence.entity.Reservation;
import com.example.tennis.persistence.entity.Surface;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

class EntityMapperTest {

    @Test
    public void testNullCourt() {
        CourtDTO courtDto = null;
        Court court = null;
        Assertions.assertThat(EntityMapper.INSTANCE.toEntity(courtDto)).isNull();
        Assertions.assertThat(EntityMapper.INSTANCE.toDto(court)).isNull();
        Court court2 = new Court();
        Assertions.assertThat(EntityMapper.INSTANCE.toDto(court2)).isNotNull();
        court2.setSurface(new Surface());
        Assertions.assertThat(EntityMapper.INSTANCE.toDto(court2).getSurface()).isNull();
    }

    @Test
    public void testNullReservation() {
        ReservationDTO reservationDTO = null;
        Reservation reservation = null;
        Assertions.assertThat(EntityMapper.INSTANCE.toEntity(reservationDTO)).isNull();
        Assertions.assertThat(EntityMapper.INSTANCE.toDto(reservation)).isNull();
        Reservation reservation2 = new Reservation();
        Assertions.assertThat(EntityMapper.INSTANCE.toDto(reservation2)).isNotNull();
    }

    @Test
    public void testReservation() {
        Court court = TestTool.createCourt("Zeee court","Flat surface",150L);
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt1","Singles",
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),"777111222", "FrantaJetel",null);
        var result = EntityMapper.INSTANCE.toEntity(reservationRequest);
        System.err.println(result);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getStartTime()).isEqualTo(reservationRequest.getStartTime());
        Assertions.assertThat(result.getEndTime()).isEqualTo(reservationRequest.getEndTime());

        var reservation = TestTool.createReservation(court,"Franta","777101101");
        reservation.setGlobalId(UUID.fromString("ab36a118-6c30-4e1a-b49a-2e6eeb65cd83"));
        GameType gameType = new GameType();
        ReflectionTestUtils.setField(gameType,"name","GameType1");
        ReflectionTestUtils.setField(gameType,"priceMultiplier",1.5f);
        reservation.setGameType(gameType);
        var result2 = EntityMapper.INSTANCE.toDto(reservation);
        Assertions.assertThat(result2).isNotNull();
        System.err.println(result2);
    }

    @Test
    public void testReservationFailCalculate() {
        var reservation = createReservation();
        reservation.setEndTime(null);
        var result = EntityMapper.INSTANCE.toDto(reservation);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getPrice()).isNull();
    }

    @Test
    public void testReservationFailCalculate2() {
        var reservation = createReservation();
        reservation.setCourt(null);
        var result = EntityMapper.INSTANCE.toDto(reservation);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getPrice()).isNull();
    }

    @Test
    public void testReservationFailCalculate3() {
        var reservation = createReservation();
        reservation.setGameType(null);
        var result = EntityMapper.INSTANCE.toDto(reservation);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getPrice()).isNull();
    }

    @Test
    public void testReservationFailCalculate5() {
        var reservation = createReservation();
        ReflectionTestUtils.setField(reservation.getGameType(),"priceMultiplier",0);
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
                () -> EntityMapper.INSTANCE.toDto(reservation));
    }

    @Test
    public void testReservationFailCalculate6() {
        var reservation = createReservation();
        ReflectionTestUtils.setField(reservation.getCourt().getSurface(), "pricePerMinute", 0L);
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
                () -> EntityMapper.INSTANCE.toDto(reservation));
    }

    @Test
    public void testReservationFailCalculate7() {
        var reservation = createReservation();
        reservation.setStartTime(LocalDateTime.now());
        reservation.setEndTime(LocalDateTime.now());
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
                () -> EntityMapper.INSTANCE.toDto(reservation));
    }

    private Reservation createReservation(){
        Court court = TestTool.createCourt("Zeee court","Flat surface",150L);
        var reservation = TestTool.createReservation(court,"Franta","777101101");
        reservation.setGlobalId(UUID.fromString("ab36a118-6c30-4e1a-b49a-2e6eeb65cd83"));
        GameType gameType = new GameType();
        ReflectionTestUtils.setField(gameType,"name","GameType1");
        ReflectionTestUtils.setField(gameType,"priceMultiplier",1.5f);
        reservation.setGameType(gameType);
        return reservation;
    }
}

