package com.example.tennis.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Immutable;

/**
 * This entity/table is designed as readonly (no changes from code)
 * - also has no global id and softdelete - can be changed in future
 */
@Entity
@Getter
@ToString
@Immutable
public class GameType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "NAME", updatable = false)
    private String name;

    @NotNull
    @Column(name = "PRICE_MULTIPLIER")
    private float priceMultiplier;

}
