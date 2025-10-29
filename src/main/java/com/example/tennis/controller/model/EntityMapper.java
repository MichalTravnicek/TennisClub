package com.example.tennis.controller.model;

import com.example.tennis.persistence.entity.Court;
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

}
