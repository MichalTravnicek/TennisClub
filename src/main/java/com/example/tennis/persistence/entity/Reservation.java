package com.example.tennis.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@ToString(callSuper = true)
public class Reservation extends BaseEntity{

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "court_id", nullable = false)
    @NotNull
    private Court court;

    @ManyToOne
    @JoinColumn(name = "game_type_id")
    @NotNull
    private GameType gameType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public Double getPrice(){
        if (startTime == null || endTime == null || gameType == null || court == null){
            return null;
        }
        var multiplier = gameType.getPriceMultiplier();
        var basePrice = court.getSurface().getPricePerMinute();
        long durationInMinutes = getStartTime().until(getEndTime(), ChronoUnit.MINUTES);
        if (!(multiplier > 0) || !(durationInMinutes > 0) || !(basePrice > 0)){
            log.error("Cannot calculate total price: multiplier:" + multiplier +
                    " duration:" + durationInMinutes + " price:" + basePrice);
            throw new ArithmeticException("Cannot calculate price");
        }

        return gameType.getPriceMultiplier()*court.getSurface().getPricePerMinute() * durationInMinutes;
    }
}
