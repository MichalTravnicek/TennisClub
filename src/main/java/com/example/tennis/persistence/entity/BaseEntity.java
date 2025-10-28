package com.example.tennis.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.UUID;

@MappedSuperclass
@Data
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP)
public class BaseEntity {

    @PrePersist
    public void persist(){
        globalId = UUID.randomUUID();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private UUID globalId;

}
