package com.example.tennis.controller.model;

import jakarta.validation.GroupSequence;

@GroupSequence({UpdateReservation.Group.class})
public interface UpdateReservation {
    interface Group {
    }
}
