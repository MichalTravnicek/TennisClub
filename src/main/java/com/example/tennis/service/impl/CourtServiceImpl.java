package com.example.tennis.service.impl;

import com.example.tennis.controller.model.CourtDTO;
import com.example.tennis.controller.model.EntityMapper;
import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.Surface;
import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.persistence.repository.SearchDAO;
import com.example.tennis.persistence.repository.TennisDAO;
import com.example.tennis.service.CourtService;
import com.example.tennis.service.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final TennisDAO repository;
    private final SearchDAO search;

    @Override
    public List<CourtDTO> getAllCourts() {
        var mapper = EntityMapper.INSTANCE;
        return repository.getAll(Court.class).stream().map(mapper::toDto).toList();
    }

    @Override
    public CourtDTO getCourt(CourtDTO court) {
        return EntityMapper.INSTANCE.toDto(findByIdOrName(court));
    }

    private Court findByIdOrName(CourtDTO court){
        try {
            if (court.getGlobalId() != null){
                return repository.getByProperty(Court.class, "globalId", court.getGlobalId());
            }
            else if (StringUtils.hasLength(court.getName())){
                return repository.getByProperty(Court.class, "name", court.getName());
            }
            throw new NotFoundException("No court id or name specified");
        } catch (NotFoundException e) {
            throw new NotFoundException("Court: " + court.getGlobalId() + " " + court.getName() + " not found");
        }
    }

    @Transactional
    @Override
    public CourtDTO createCourt(CourtDTO court) {
        Court courtEntity = EntityMapper.INSTANCE.toEntity(court);
        var existingCourt = repository.findByProperty(Court.class, "name", court.getName());
        if (existingCourt.isPresent()) {
            throw new ConflictException("Court with name:" + court.getName() + " already exists!");
        }
        var existingSurface = repository.findByProperty(Surface.class, "name", court.getSurface());
        if (existingSurface.isEmpty()) {
            throw new NotFoundException("Surface does not exist: " + court.getSurface());
        }
        courtEntity.setGlobalId(null);
        courtEntity.setSurface(existingSurface.get());
        log.info("Creating court:" + court.getName());
        repository.save(courtEntity);
        return EntityMapper.INSTANCE.toDto(courtEntity);
    }

    @Transactional
    @Override
    public CourtDTO updateCourt(CourtDTO court) {
        var existingCourt = repository.findByProperty(Court.class,"globalId",court.getGlobalId());
        if (existingCourt.isEmpty()) {
            throw new NotFoundException("Court with id: " + court.getGlobalId() + " not found");
        }

        log.info("Updating court:" + court.getName());

        var updatedCourt = existingCourt.get();
        if (StringUtils.hasLength(court.getName()) && !court.getName().equals(updatedCourt.getName())){
            var existingName = repository.findByProperty(Court.class, "name", court.getName());
            if (existingName.isPresent()){
                throw new ConflictException("Court with name:" + court.getName() + " already exists");
            }
            updatedCourt.setName(court.getName());
        }

        if (StringUtils.hasLength(court.getSurface())){
            var existingSurface = repository.findByProperty(Surface.class, "name", court.getSurface());
            if (existingSurface.isEmpty()) {
                throw new NotFoundException("Surface does not exist: " + court.getSurface());
            }
            updatedCourt.setSurface(existingSurface.get());
            repository.save(updatedCourt);
        }

        return EntityMapper.INSTANCE.toDto(updatedCourt);
    }

    @Transactional
    @Override
    public void deleteCourt(CourtDTO court) {
        Court courtEntity = findByIdOrName(court);
        var reservations = search.getReservationsForCourt(court.getName());
        if (!reservations.isEmpty()){
           throw new ConflictException("Court: " + courtEntity.getName() +
                   " is used in reservations");
        }
        repository.delete(courtEntity);
    }

}
