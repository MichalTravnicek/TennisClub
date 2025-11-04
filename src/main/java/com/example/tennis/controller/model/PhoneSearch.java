package com.example.tennis.controller.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneSearch {

    @Pattern(regexp = "^(?:\\+\\d\\d\\d\\s?)?[1-9][0-9]{2}\\s?[0-9]{3}\\s?[0-9]{3}$")
    @NotNull
    private String phone;

    private boolean future;

}
