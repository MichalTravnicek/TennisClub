package com.example.tennis.persistence.repository;

import com.example.tennis.persistence.entity.Reservation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchDAO {

    Boolean testScheduleOverlap(@Nonnull String courtName, @Nonnull LocalDateTime fromTime, @Nonnull LocalDateTime toTime);

    List<Reservation> getReservationsForCourt(@Nonnull String courtName);

    List<Reservation> getReservationsForPhone(@Nonnull String phoneNumber, @Nullable LocalDateTime fromTime);

}
