package com.example.tennis.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    @NotNull(groups = UpdateReservation.Group.class)
    private UUID globalId;

    @NotNull(groups = CreateReservation.Group.class)
    private String court;

    @NotNull(groups = CreateReservation.Group.class)
    private String gameType;

    @NotNull(groups = CreateReservation.Group.class)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(groups = CreateReservation.Group.class)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private LocalDateTime created;

    @NotNull(groups = CreateReservation.Group.class)
    @Pattern(regexp = "^(?:\\+\\d\\d\\d\\s?)?[1-9][0-9]{2}\\s?[0-9]{3}\\s?[0-9]{3}$",
        groups = CreateReservation.Group.class)
    private String phone;

    private String customer;

    private Float price;

}
