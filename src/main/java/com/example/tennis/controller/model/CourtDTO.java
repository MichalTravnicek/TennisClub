package com.example.tennis.controller.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtDTO {

    private UUID globalId;

    @NotNull
    private String name;

    private String surface;

}
