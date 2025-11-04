package com.example.tennis.controller.model;

import jakarta.validation.GroupSequence;

@GroupSequence({CreateReservation.Group.class})
public interface CreateReservation {
    interface Group {
    }
}
