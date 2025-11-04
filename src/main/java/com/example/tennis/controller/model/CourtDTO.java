package com.example.tennis.controller.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtDTO {

    @NotNull(groups = UpdateCourt.Group.class)
    private UUID globalId;

    @NotNull(groups = CreateCourt.Group.class)
    @Size(min = 1, max = 50, groups = CreateCourt.Group.class)
    private String name;

    @NotNull(groups = CreateCourt.Group.class)
    @Size(min = 1, max = 50, groups = CreateCourt.Group.class)
    private String surface;

}
