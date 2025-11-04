package com.example.tennis.controller.model;

import jakarta.validation.GroupSequence;

@GroupSequence({CreateCourt.Group.class})
public interface CreateCourt {
    interface Group {
    }
}
