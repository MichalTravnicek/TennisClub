package com.example.tennis.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends BaseEntity{

    @NotNull
    private String name;

    @Column(unique = true)
    @NotNull
    @Pattern(regexp = "^(?:\\+\\d\\d\\d\\s?)?[1-9][0-9]{2}\\s?[0-9]{3}\\s?[0-9]{3}$")
    private String phone;

}
