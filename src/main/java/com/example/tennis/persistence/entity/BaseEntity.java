package com.example.tennis.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Data
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP)
public class BaseEntity {

    @PrePersist
    public void persist(){
        if (globalId == null) {
            globalId = UUID.randomUUID();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private UUID globalId;

    @CreationTimestamp
    private LocalDateTime created;

}
