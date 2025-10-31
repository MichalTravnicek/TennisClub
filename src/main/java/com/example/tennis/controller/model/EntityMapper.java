package com.example.tennis.controller.model;

import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    @Mapping(target = "surface", ignore = true)
    Court toEntity(CourtDTO dto);

    @Mapping(target = "surface", source = "surface.name")
    CourtDTO toDto(Court entity);

    @Mapping(target = "court", ignore = true)
    @Mapping(target = "gameType", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Reservation toEntity(ReservationDTO dto);

    @Mapping(target = "court", source = "court.name")
    @Mapping(target = "gameType", source = "gameType.name")
    @Mapping(target = "customer", source = "customer.name")
    @Mapping(target = "phone", source = "customer.phone")
    ReservationDTO toDto(Reservation entity);
}
