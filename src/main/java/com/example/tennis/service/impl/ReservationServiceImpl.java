package com.example.tennis.service.impl;

import com.example.tennis.controller.model.CourtSearch;
import com.example.tennis.controller.model.EntityMapper;
import com.example.tennis.controller.model.PhoneSearch;
import com.example.tennis.controller.model.ReservationDTO;
import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.Customer;
import com.example.tennis.persistence.entity.GameType;
import com.example.tennis.persistence.entity.Reservation;
import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.persistence.repository.SearchDAO;
import com.example.tennis.persistence.repository.TennisDAO;
import com.example.tennis.service.ReservationService;
import com.example.tennis.service.exception.BadArgumentException;
import com.example.tennis.service.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final TennisDAO repository;
    private final SearchDAO search;
    private EntityMapper mapper = EntityMapper.INSTANCE;

    @Override
    public List<ReservationDTO> getAllReservations() {
        return repository.getAll(Reservation.class).stream()
                .map(this::calculateCost)
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ReservationDTO> getAllReservationsForCourt(CourtSearch court) {
        repository.getByProperty(Court.class, "name", court.getName());
        return search.getReservationsForCourt(court.getName()).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<ReservationDTO> getAllReservationsForPhone(PhoneSearch phoneSearch) {
        repository.getByProperty(Customer.class, "phone", phoneSearch.getPhone());
        var fromTime = phoneSearch.isFuture() ? LocalDateTime.now() : null;
        return search.getReservationsForPhone(phoneSearch.getPhone(),fromTime).stream().map(mapper::toDto).toList();
    }

    @Override
    public ReservationDTO getReservation(ReservationDTO reservation) {
        return mapper.toDto(calculateCost(getByGlobalId(reservation)));
    }

    private Reservation getByGlobalId(ReservationDTO reservation){
        if (reservation.getGlobalId() != null) {
            var result = repository.findByProperty(Reservation.class, "globalId", reservation.getGlobalId());
            return result.orElseThrow(() -> new NotFoundException("Reservation: " + reservation.getGlobalId() + " not found"));
        }
        throw new NotFoundException("No reservation id specified");
    }

    private Reservation calculateCost(Reservation reservation) {
        float multiplier = reservation.getGameType().getPriceMultiplier();
        long durationInMinutes = reservation.getStartTime().until(reservation.getEndTime(), ChronoUnit.MINUTES);
        long price = reservation.getCourt().getSurface().getPricePerMinute();
        if (!(multiplier > 0) || !(durationInMinutes > 0) || !(price > 0)){
            log.error("Cannot calculate total price: multiplier:"+multiplier +
                    " duration:"+ durationInMinutes + " price:"+price);
            throw new ArithmeticException("Cannot calculate price");
        }
        reservation.setPrice(price * multiplier * durationInMinutes);
        return reservation;
    }
    
    private void validateTimeProperties(ReservationDTO reservation, Long excludeId){
        if (!reservation.getStartTime().isBefore(reservation.getEndTime())){
            throw new BadArgumentException("End time :" + reservation.getEndTime() +
                    " is not after start time" + reservation.getStartTime());
        }
        var overlap = search.testScheduleOverlap(reservation.getCourt(),
                reservation.getStartTime(),reservation.getEndTime(), excludeId);
        if (overlap) {
            throw new ConflictException("Court reservation is in conflict with existing reservation");
        }
    }

    private void validateStringPropertyPresent(String name, String value){
        if (!StringUtils.hasLength(value)){
            log.error(name +" is missing in the request");
            throw new BadArgumentException(name + " is missing in the request");
        }
    }

    @Transactional
    @Override
    public ReservationDTO createReservation(ReservationDTO reservation) {

        validateStringPropertyPresent("Court name", reservation.getCourt());
        validateStringPropertyPresent("Customer phone", reservation.getPhone());
        validateStringPropertyPresent("Game type", reservation.getGameType());

        Reservation entity = mapper.toEntity(reservation);

        trySetProperty(reservation.getCourt(), "", Court.class,"name", entity::setCourt);
        trySetProperty(reservation.getGameType(), "", GameType.class,"name", entity::setGameType);

        validateTimeProperties(reservation, null);

        var existingCustomer = repository.findByProperty(Customer.class, "phone", reservation.getPhone());

        if (existingCustomer.isPresent()) {
            entity.setCustomer(existingCustomer.get());
        } else {
            Customer customer = new Customer();
            customer.setPhone(reservation.getPhone());
            var name = StringUtils.hasLength(reservation.getCustomer()) ? reservation.getCustomer() : "Unknown";
            customer.setName(name);
            entity.setCustomer(customer);
        }

        entity.setGlobalId(null);
        log.info("Creating reservation:");
        repository.save(entity);
        var pricedEntity = calculateCost(entity);
        log.info("Created:" + pricedEntity);
        return mapper.toDto(pricedEntity);
    }

    private <T> void trySetProperty(String newValue, String oldValue, Class<T> entityClass,
                                    String property, Consumer<T> setter){
        if (StringUtils.hasLength(newValue) && !newValue.equals(oldValue)){
            var existing = repository.findByProperty(entityClass, property, newValue);
            if (existing.isEmpty()) {
                throw new BadArgumentException(entityClass.getSimpleName() + ":" + newValue + " not found!");
            }
            setter.accept(existing.get());
        }
    }

    @Transactional
    @Override
    public ReservationDTO updateReservation(ReservationDTO reservation) {
        var existingEntity = getByGlobalId(reservation);

        trySetProperty(reservation.getCourt(), existingEntity.getCourt().getName(),
                Court.class,"name", existingEntity::setCourt);

        trySetProperty(reservation.getPhone(), existingEntity.getCustomer().getPhone(),
                Customer.class,"phone", existingEntity::setCustomer);

        trySetProperty(reservation.getCustomer(), existingEntity.getCustomer().getName(),
                Customer.class,"name", existingEntity::setCustomer);

        trySetProperty(reservation.getGameType(), existingEntity.getGameType().getName(),
                GameType.class,"name", existingEntity::setGameType);

        if (StringUtils.hasLength(reservation.getCourt())
                && reservation.getStartTime() != null && reservation.getEndTime() != null
                && (!existingEntity.getStartTime().equals(reservation.getStartTime())
                || !existingEntity.getEndTime().equals(reservation.getEndTime()))
        ) {
            validateTimeProperties(reservation, existingEntity.getId());
            existingEntity.setStartTime(reservation.getStartTime());
            existingEntity.setEndTime(reservation.getEndTime());
        }

        log.info("Updating reservation:");
        repository.save(existingEntity);
        var pricedEntity = calculateCost(existingEntity);
        log.info("Updated:" + pricedEntity);
        return mapper.toDto(pricedEntity);
    }

    @Transactional
    @Override
    public void deleteReservation(ReservationDTO reservation) {
        var existingEntity = getByGlobalId(reservation);
        repository.delete(existingEntity);
    }
}
