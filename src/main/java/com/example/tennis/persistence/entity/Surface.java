package com.example.tennis.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
public class Surface extends BaseEntity{

    @NotNull
    @Size(min = 1, max = 50)
    @Column(unique = true)
    private String name;

    @NotNull
    @Column(name = "PRICE_PER_MINUTE")
    private Long pricePerMinute;

}
