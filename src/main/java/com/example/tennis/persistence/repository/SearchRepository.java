package com.example.tennis.persistence.repository;

import com.example.tennis.persistence.entity.Reservation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SearchRepository implements SearchDAO{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Boolean testScheduleOverlap(@Nonnull String courtName, @Nonnull LocalDateTime fromTime, @Nonnull LocalDateTime toTime){
        return entityManager.createQuery("SELECT EXISTS (SELECT 1 FROM Reservation r " +
                        "WHERE (r.court.name = :courtName) " +
                        "AND (:fromTime < r.endTime AND r.startTime < :toTime))", Boolean.class)
                .setParameter("courtName", courtName)
                .setParameter("fromTime", fromTime)
                .setParameter("toTime", toTime)
                .getSingleResult();
    }

    @Override
    public List<Reservation> getReservationsForCourt(@Nonnull String courtName){
        return entityManager.createQuery("SELECT r FROM Reservation r " +
                        "WHERE (r.court.name = :courtName) " +
                        "ORDER BY r.startTime ASC", Reservation.class)
                .setParameter("courtName", courtName)
                .getResultList();
    }

    @Override
    public List<Reservation> getReservationsForPhone(@Nonnull String phoneNumber, @Nullable LocalDateTime fromTime){
        return entityManager.createQuery("SELECT r FROM Reservation r " +
                        "WHERE (r.customer.phone = :phone) " +
                        "AND (:time is NULL OR r.startTime > :time) " +
                        "ORDER BY r.startTime ASC", Reservation.class)
                .setParameter("phone", phoneNumber)
                .setParameter("time", fromTime)
                .getResultList();
    }
}
