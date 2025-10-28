package com.example.tennis.persistence;

import com.example.tennis.persistence.entity.*;
import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.persistence.repository.TennisDAO;
import com.example.tennis.persistence.repository.TennisRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

@Import(TennisRepository.class)
@DataJpaTest
class TennisRepositoryTest {

    @Autowired
    TennisDAO repository;

    @Autowired
    EntityManager entityManager;

    @Test
    public void testSaveCourt(){
        Surface surface = new Surface();
        surface.setName("Test surface");
        surface.setPricePerMinute(123L);
        Court court = new Court();
        court.setName("Test Court");
        court.setSurface(surface);
        repository.save(court);
        entityManager.flush();
        var id = court.getId();
        entityManager.clear();
        var result = repository.getById(Court.class,id);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getGlobalId()).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("Test Court");
        Assertions.assertThat(result.getSurface()).isNotNull();
        Assertions.assertThat(result.getSurface().getName()).isEqualTo("Test surface");
        Assertions.assertThat(result.getSurface().getPricePerMinute()).isEqualTo(123L);
    }

    @Test
    public void testCreateCustomer(){
        Customer customer = new Customer();
        customer.setPhone("777123123");
        customer.setName("TestUser5");
        repository.save(customer);
        entityManager.flush();
        var id = customer.getId();
        entityManager.clear();
        var result = repository.getById(Customer.class,id);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("TestUser5");
        Assertions.assertThat(result.getPhone()).isEqualTo("777123123");
    }

    @Test
    public void testPhoneValidation(){
        Customer customer = new Customer();
        customer.setPhone("777 125 125");
        customer.setName("TestUser2");
        repository.save(customer);
        Customer customer2 = new Customer();
        customer2.setPhone("+420 777 125 125");
        customer2.setName("TestUser6");
        repository.save(customer2);
        Customer customer3 = new Customer();
        customer3.setPhone("777 XXX XXX");
        customer3.setName("TestUser3");
        org.junit.jupiter.api.Assertions.assertThrows(
                ConstraintViolationException.class,
                () -> repository.save(customer3)
        );
    }

    @Test
    public void testGametype(){
        var gametypes = repository.getAll(GameType.class);
        System.err.println(gametypes);
        Assertions.assertThat(gametypes).isNotNull();
        Assertions.assertThat(gametypes.getFirst().getName()).isNotNull();
        Assertions.assertThat(gametypes.getFirst().getPriceMultiplier()).isGreaterThan(0);
    }

    @Test
    public void testMerge(){
        Court court = TestTool.createCourt("TestCourt15", "TestSurface15", 100L);
        repository.save(court);
        flushAndClear();
        var saved = repository.getByProperty(Court.class,"name","TestCourt15");
        entityManager.clear();
        saved.setName("SomeOtherName");
        repository.save(saved);
        flushAndClear();
        var saved2 = repository.getByProperty(Court.class,"name","SomeOtherName");
        Assertions.assertThat(saved2).isNotNull();
    }

    @Test
    public void testFailFind() {
        var result = repository.findById(GameType.class, 100L);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    public void testFailGet(){
        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class, () ->
                repository.getById(GameType.class,100L));
    }

    @Test
    public void testFailGetByProperty(){
        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class, () ->
                        repository.getByProperty(GameType.class,"unknown",0));
    }

    @Test
    public void testFailGetByValidProperty(){
        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class, () ->
                        repository.getByProperty(GameType.class,"name","bad")
        );
    }

    @Test
    public void testSaveDuplicatedItem(){
        Customer customer = new Customer();
        customer.setName("Michal8");
        customer.setPhone("777125444");
        repository.save(customer);
        flushAndClear();
        Customer customer2 = new Customer();
        customer2.setName("Michal9");
        customer2.setPhone("777125444");

        System.err.println(repository.getAll(Customer.class));
        org.junit.jupiter.api.Assertions.assertThrows(
                org.hibernate.exception.ConstraintViolationException.class, () ->
                        repository.save(customer2)
        );
    }

    @Test
    public void testUpdateCourt(){
        Surface surface = new Surface();
        surface.setName("Test surface7");
        surface.setPricePerMinute(123L);
        Court court = new Court();
        court.setName("Test Court7");
        court.setSurface(surface);
        repository.save(court);
        entityManager.flush();
        var id = court.getId();
        entityManager.clear();
        var result = repository.getById(Court.class,id);
        result.setName("TestCourt7.2");
        Surface surface2 = new Surface();
        surface2.setName("Test surface9");
        surface2.setPricePerMinute(150L);
        result.setSurface(surface2);
        flushAndClear();
        var result2 = repository.getById(Court.class,id);
        Assertions.assertThat(result2).isNotNull();
        Assertions.assertThat(result2.getName()).isEqualTo("TestCourt7.2");
        Assertions.assertThat(result2.getSurface()).isNotNull();
        Assertions.assertThat(result2.getSurface().getName()).isEqualTo("Test surface9");
        Assertions.assertThat(result2.getSurface().getPricePerMinute()).isEqualTo(150L);
    }

    @Test
    public void testCreateReservation(){
        Court court = TestTool.createCourt("TestCourt5", "TestSurface2", 100L);

        Reservation reservation = new Reservation();
        reservation.setCourt(court);
        Customer customer = new Customer();
        customer.setName("MichalTest");
        customer.setPhone("777123000");
        reservation.setCustomer(customer);
        var gameTypes = repository.getAll(GameType.class);
        reservation.setGameType(gameTypes.getFirst());
        var start = LocalDateTime.of(2045, 5, 1, 0, 0);
        var end = LocalDateTime.of(2045, 5, 2, 0, 0);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        repository.save(reservation);
        flushAndClear();

        var savedReservation = repository.getAll(Reservation.class);
        var savedCourts = repository.getAll(Court.class);

        System.err.println(savedReservation);
        System.err.println(savedCourts);

        Assertions.assertThat(savedReservation).isNotNull();
        Assertions.assertThat(savedReservation.getFirst()).isNotNull();
        Assertions.assertThat(savedReservation.getFirst().getStartTime()).isEqualTo(start);
        Assertions.assertThat(savedReservation.getFirst().getEndTime()).isEqualTo(end);
        Assertions.assertThat(savedReservation.getFirst().getGameType()).isNotNull();
        Assertions.assertThat(savedReservation.getFirst().getGameType().getPriceMultiplier())
                .isGreaterThan(0);
        Assertions.assertThat(savedReservation.getFirst().getCustomer()).isNotNull();
        Assertions.assertThat(savedReservation.getFirst().getCustomer().getName())
                .isEqualTo("MichalTest");
        Assertions.assertThat(savedReservation.getFirst().getCustomer().getPhone())
                .isEqualTo("777123000");
        Assertions.assertThat(savedReservation.getFirst().getCourt()).isNotNull();
        Assertions.assertThat(savedReservation.getFirst().getCourt().getName())
                .isEqualTo("TestCourt5");
        Assertions.assertThat(savedReservation.getFirst().getCourt().getSurface().getName())
                .isEqualTo("TestSurface2");
        Assertions.assertThat(savedReservation.getFirst().getCourt().getSurface().getPricePerMinute())
                .isEqualTo(100L);
    }

    @Test
    public void testDeleteReservation(){
        Court court = TestTool.createCourt("TestCourt12", "TestSurface12", 100L);
        repository.save(court);

        var reservation = TestTool.createReservation(court,"MichalTestDelete","777123890");
        var gameTypes = repository.getAll(GameType.class);
        reservation.setGameType(gameTypes.getFirst());
        repository.save(reservation);
        flushAndClear();

        var allReservationsSize = repository.getAll(Reservation.class).size();
        var savedReservation = repository.findByProperty(Reservation.class,"customer.phone","777123890");
        Assertions.assertThat(savedReservation).isPresent();
        repository.delete(savedReservation.get());
        flushAndClear();

        var allReservationsAfter = repository.getAll(Reservation.class).size();
        Assertions.assertThat(allReservationsAfter).isEqualTo(allReservationsSize -1);
        var deletedReservation = repository.findByProperty(Reservation.class,"customer.phone","777123890");
        Assertions.assertThat(deletedReservation).isNotPresent();
        var realCountAfter = entityManager.createNativeQuery("SELECT COUNT(*) FROM RESERVATION", Integer.class).getSingleResult();
        Assertions.assertThat(realCountAfter).isEqualTo(allReservationsSize);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
