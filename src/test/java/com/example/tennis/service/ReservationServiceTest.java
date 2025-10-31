package com.example.tennis.service;

import com.example.tennis.controller.model.CourtSearch;
import com.example.tennis.controller.model.EntityMapper;
import com.example.tennis.controller.model.PhoneSearch;
import com.example.tennis.controller.model.ReservationDTO;
import com.example.tennis.persistence.TestTool;
import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.Customer;
import com.example.tennis.persistence.entity.GameType;
import com.example.tennis.persistence.entity.Reservation;
import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.persistence.repository.SearchRepository;
import com.example.tennis.persistence.repository.TennisDAO;
import com.example.tennis.persistence.repository.TennisRepository;
import com.example.tennis.service.exception.BadArgumentException;
import com.example.tennis.service.exception.ConflictException;
import com.example.tennis.service.impl.ReservationServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({TennisRepository.class,
        SearchRepository.class,
        ReservationServiceImpl.class,
})
@DataJpaTest
class ReservationServiceTest {

    @Autowired
    private TennisDAO repository;

    @Autowired
    private ReservationService service;

    private GameType getTestGameType() {
        return repository.getAll(GameType.class).getFirst();
    }

    @Test
    public void getReservation() {
        Court court = TestTool.createCourt("ReservationCourt1", "DirtAAA", 100L);
        repository.save(court);
        Reservation reservation = TestTool.createReservation(court, "Petr", "777147852");
        GameType gameType = getTestGameType();
        reservation.setGameType(gameType);
        repository.save(reservation);
        var request = new ReservationDTO();
        request.setGlobalId(reservation.getGlobalId());
        var result = service.getReservation(request);
        System.err.println(result);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getStartTime()).isEqualTo(reservation.getStartTime());
        Assertions.assertThat(result.getEndTime()).isEqualTo(reservation.getEndTime());
        Assertions.assertThat(result.getGlobalId()).isEqualTo(reservation.getGlobalId());
        Assertions.assertThat(result.getCustomer()).isEqualTo(reservation.getCustomer().getName());
        Assertions.assertThat(result.getPhone()).isEqualTo(reservation.getCustomer().getPhone());
        Assertions.assertThat(result.getCourt()).isEqualTo(reservation.getCourt().getName());
        Assertions.assertThat(result.getGameType()).isEqualTo(reservation.getGameType().getName());
    }

    @Test
    public void testCalculationFailed(){
        Court court = TestTool.createCourt("ReservationCourt1", "DirtAAA", 0L);
        Reservation reservation = TestTool.createReservation(court,"Pepa","777147000");
        GameType gameType = getTestGameType();
        reservation.setGameType(gameType);
        repository.save(reservation);
        assertThrows(ArithmeticException.class,
                () -> ReflectionTestUtils.invokeMethod(service,"calculateCost",reservation));
    }

    @Test
    public void createReservation() {
        Court court = TestTool.createCourt("ReservationCourt1", "DirtAAA", 100L);
        repository.save(court);

        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt1","Singles",
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777111333", "FrantaJetel2", null);

        var result = service.createReservation(reservationRequest);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getGlobalId()).isNotNull();
        Assertions.assertThat(result.getGameType()).isEqualTo("Singles");
        Assertions.assertThat(result.getCustomer()).isEqualTo("FrantaJetel2");
        Assertions.assertThat(result.getPhone()).isEqualTo("777111333");
        Assertions.assertThat(result.getCourt()).isEqualTo("ReservationCourt1");
        Assertions.assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2025,12,12,0,0));
        Assertions.assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2025,12,15,0,0));
        Assertions.assertThat(result.getPrice()).isEqualTo(100*4320*1.0f);
    }

    @Test
    public void createReservationIncomplete() {
        ReservationDTO reservationRequest = new ReservationDTO(
                null,null,null,
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777555333", "FrantaJetel299", null);

        assertThrows(BadArgumentException.class,
                ()-> service.createReservation(reservationRequest));
    }

    @Test
    public void createReservationBadCourt() {
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"BadCourt","Singles",
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777555333", "FrantaJetel299", null);

        assertThrows(BadArgumentException.class,
                ()-> service.createReservation(reservationRequest));
    }

    @Test
    public void createReservationBadGameType() {
        Court court = TestTool.createCourt("ReservationCourt567", "DirtAAA", 100L);
        repository.save(court);
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt567","BadGametype",
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777555333", "FrantaJetel299", null);

        assertThrows(BadArgumentException.class,
                ()-> service.createReservation(reservationRequest));
    }

    @Test
    public void createReservationOverlap() {
        Court court = TestTool.createCourt("ReservationCourt9", "DirtFFF", 100L);
        repository.save(court);

        GameType gameType = getTestGameType();
        Reservation reservation = TestTool.createReservation(court,"PepaZDepa","777951951");
        reservation.setGameType(gameType);
        reservation.setStartTime(LocalDateTime.of(2025,12,12,0,0));
        reservation.setEndTime(LocalDateTime.of(2025,12,13,0,0));
        repository.save(reservation);

        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt9","Singles",
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777222333", "FrantaJetel299", null);

        assertThrows(ConflictException.class,
                ()-> service.createReservation(reservationRequest));
    }

    @Test
    public void createReservationBadTimeRange() {
        Court court = TestTool.createCourt("ReservationCourt29", "DirtGGG", 100L);
        repository.save(court);

        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt29","Singles",
                LocalDateTime.of(2025,12,16,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777888333", "FrantaJetel899", null);

        assertThrows(BadArgumentException.class,
                ()-> service.createReservation(reservationRequest));
    }

    @Test
    public void updateReservation() {
        Court court = TestTool.createCourt("ReservationCourt12", "DirtBBB", 100L);
        repository.save(court);
        Court court2 = TestTool.createCourt("ReservationCourt112", "DirtYYY", 10L);
        repository.save(court2);

        Customer customer = new Customer("FrantaJetel3","777111333");
        repository.save(customer);

        Reservation reservation = TestTool.createReservation(court,"PanKlobouk","777111222");
        reservation.setStartTime(LocalDateTime.of(2025,12,16,0,0));
        reservation.setEndTime(LocalDateTime.of(2025,12,17,0,0));
        GameType gameType = getTestGameType();
        reservation.setGameType(gameType);
        repository.save(reservation);

        ReservationDTO reservationRequest = new ReservationDTO(
                reservation.getGlobalId(),"ReservationCourt112","Doubles",
                LocalDateTime.of(2025,12,16,0,0),
                LocalDateTime.of(2025,12,19,0,0),
                "777111333", "FrantaJetel3", null);

        var result = service.updateReservation(reservationRequest);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getGlobalId()).isNotNull();
        Assertions.assertThat(result.getGameType()).isEqualTo("Doubles");
        Assertions.assertThat(result.getCustomer()).isEqualTo("FrantaJetel3");
        Assertions.assertThat(result.getPhone()).isEqualTo("777111333");
        Assertions.assertThat(result.getCourt()).isEqualTo("ReservationCourt112");
        Assertions.assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2025,12,16,0,0));
        Assertions.assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2025,12,19,0,0));
        Assertions.assertThat(result.getPrice()).isEqualTo(10*4320*1.5f);
    }

    @Test
    public void updateReservationOverlap() {
        Court court = TestTool.createCourt("ReservationCourt12", "Dirty", 200L);
        repository.save(court);

        Customer customer = new Customer("FrantaJetel256","777111333");
        repository.save(customer);
        GameType gameType = getTestGameType();

        Reservation reservation = TestTool.createReservation(court,customer,gameType);
        reservation.setStartTime(LocalDateTime.of(2025,12,16,0,0));
        reservation.setEndTime(LocalDateTime.of(2025,12,17,0,0));
        repository.save(reservation);

        Reservation reservation2 = TestTool.createReservation(court,customer,gameType);
        reservation2.setStartTime(LocalDateTime.of(2025,12,20,0,0));
        reservation2.setEndTime(LocalDateTime.of(2025,12,25,0,0));
        repository.save(reservation2);

        ReservationDTO reservationRequest = new ReservationDTO(
                reservation.getGlobalId(),court.getName(),"Singles",
                LocalDateTime.of(2025,12,20,0,0),
                LocalDateTime.of(2025,12,25,0,0),
                "777111333", null, null);

        assertThrows(ConflictException.class,
                ()->service.updateReservation(reservationRequest));
    }

    @Test
    public void updateReservationOnlyCustomer() {
        Court court = TestTool.createCourt("ReservationCourt1235", "Dirty2", 200L);
        repository.save(court);

        Customer customer = new Customer("FrantaJetel2567","777555777");
        repository.save(customer);

        Customer customer2 = new Customer("FrantaPalicka","777111999");
        repository.save(customer2);

        GameType gameType = getTestGameType();

        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt1235","Singles",
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777555777", "FrantaJetel2567", null);

        service.createReservation(reservationRequest);
        
        Reservation reservation = TestTool.createReservation(court,customer,gameType);
        reservation.setStartTime(LocalDateTime.of(2025,12,16,0,0));
        reservation.setEndTime(LocalDateTime.of(2025,12,17,0,0));
        repository.save(reservation);

        ReservationDTO updateRequest = new ReservationDTO(
                reservation.getGlobalId(),null,"Singles",
                null,
                null,
                "777111999", null, null);

        var result = service.updateReservation(updateRequest);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCustomer()).isEqualTo(customer2.getName());
        Assertions.assertThat(result.getStartTime()).isEqualTo(reservation.getStartTime());
        Assertions.assertThat(result.getEndTime()).isEqualTo(reservation.getEndTime());
        Assertions.assertThat(result.getCourt()).isEqualTo(court.getName());
    }

    @Test
    public void updateReservationNoId() {
        ReservationDTO reservationRequest = new ReservationDTO();
        assertThrows(NotFoundException.class, () -> service.updateReservation(reservationRequest));
    }

    @Test
    public void updateReservationBadCourt() {
        Court court = TestTool.createCourt("ReservationCourt12", "DirtBBB", 100L);
        repository.save(court);
        Reservation reservation = TestTool.createReservation(court,"PanKlobouk","777111222");
        reservation.setStartTime(LocalDateTime.of(2025,12,16,0,0));
        reservation.setEndTime(LocalDateTime.of(2025,12,17,0,0));
        GameType gameType = getTestGameType();
        reservation.setGameType(gameType);
        repository.save(reservation);

        ReservationDTO reservationRequest = new ReservationDTO(
                reservation.getGlobalId(),"NonexistentCourt","Doubles",
                LocalDateTime.of(2025,12,16,0,0),
                LocalDateTime.of(2025,12,19,0,0),
                "777111333", "FrantaJetel3", null);

        assertThrows(BadArgumentException.class, () -> service.updateReservation(reservationRequest));
    }

    @Test
    public void deleteReservation() {
        Court court = TestTool.createCourt("ReservationCourt88", "DirtCCC", 100L);
        repository.save(court);
        Reservation reservation = TestTool.createReservation(court, "PanKlobouk2", "777111555");
        reservation.setStartTime(LocalDateTime.of(2025, 12, 16, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2025, 12, 17, 0, 0));
        GameType gameType = getTestGameType();
        reservation.setGameType(gameType);
        repository.save(reservation);

        ReservationDTO reservationRequest = new ReservationDTO();
        reservationRequest.setGlobalId(reservation.getGlobalId());
        service.deleteReservation(reservationRequest);
        assertThrows(NotFoundException.class,
                () -> service.getReservation(reservationRequest));
    }

    @Test
    public void deleteReservationNotFound() {
        ReservationDTO request = new ReservationDTO();
        assertThrows(NotFoundException.class,
                () -> service.deleteReservation(request));
    }

    @Test
    void getAllReservations() {
        Court court = TestTool.createCourt("ReservationCourt88", "DirtFFF", 100L);
        Reservation reservation = TestTool.createReservation(court,"PanKlobouk125","777699222");
        reservation.setStartTime(LocalDateTime.of(2025, 12, 16, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2025, 12, 17, 0, 0));
        reservation.setGameType(getTestGameType());
        repository.save(reservation);
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt88",getTestGameType().getName(),
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777555333", "FrantaJetel899", null);

        var resultSave = service.createReservation(reservationRequest);
        var repo2 = repository.findByProperty(Reservation.class,"globalId",resultSave.getGlobalId());

        var result = service.getAllReservations();
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(repo2).isPresent();
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.contains(EntityMapper.INSTANCE.toDto(reservation))).isTrue();
        Assertions.assertThat(result.contains(EntityMapper.INSTANCE.toDto(repo2.get()))).isTrue();
    }

    @Test
    void getAllReservationsForCourt() {
        Court court = TestTool.createCourt("ReservationCourt46", "DirtFFF", 100L);
        Court court2 = TestTool.createCourt("ReservationCourt246", "DirtZZZ", 100L);
        repository.save(court2);
        Reservation reservation = TestTool.createReservation(court,"PanKlobouk125","777699222");
        reservation.setStartTime(LocalDateTime.of(2025, 12, 16, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2025, 12, 17, 0, 0));
        reservation.setGameType(getTestGameType());
        repository.save(reservation);
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt246",getTestGameType().getName(),
                LocalDateTime.of(2025,12,12,0,0),
                LocalDateTime.of(2025,12,15,0,0),
                "777555333", "FrantaJetel399", null);

        var resultSave = service.createReservation(reservationRequest);

        var result = service.getAllReservationsForCourt(new CourtSearch("ReservationCourt46"));
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(resultSave).isNotNull();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.contains(EntityMapper.INSTANCE.toDto(reservation))).isTrue();
    }

    @Test
    void getAllReservationsForPhone() {
        Court court = TestTool.createCourt("ReservationCourt146", "DirtAAA", 100L);
        Court court2 = TestTool.createCourt("ReservationCourt346", "DirtUUU", 100L);
        repository.save(court2);
        Reservation reservation = TestTool.createReservation(court,"PanKlobouk425","777699222");
        reservation.setStartTime(LocalDateTime.of(2005, 12, 16, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2005, 12, 17, 0, 0));
        reservation.setGameType(getTestGameType());
        repository.save(reservation);
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt146",getTestGameType().getName(),
                LocalDateTime.of(2005,12,12,0,0),
                LocalDateTime.of(2005,12,15,0,0),
                "777555333", "FrantaJetel199", null);

        var resultSave = service.createReservation(reservationRequest);
        var repo2 = repository.findByProperty(Reservation.class,"globalId",resultSave.getGlobalId());

        var result = service.getAllReservationsForPhone(new PhoneSearch("777555333",false));
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(resultSave).isNotNull();
        Assertions.assertThat(repo2).isPresent();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.contains(EntityMapper.INSTANCE.toDto(repo2.get()))).isTrue();
    }

    @Test
    void getAllReservationsForPhoneFuture() {
        Court court = TestTool.createCourt("ReservationCourt146", "DirtAAA", 100L);
        Court court2 = TestTool.createCourt("ReservationCourt346", "DirtUUU", 100L);
        repository.save(court2);
        Reservation reservation = TestTool.createReservation(court,"PanKlobouk425","777699222");
        reservation.setStartTime(LocalDateTime.of(2005, 12, 16, 0, 0));
        reservation.setEndTime(LocalDateTime.of(2005, 12, 17, 0, 0));
        reservation.setGameType(getTestGameType());
        repository.save(reservation);
        ReservationDTO reservationRequest = new ReservationDTO(
                null,"ReservationCourt146",getTestGameType().getName(),
                LocalDateTime.of(2005,12,12,0,0),
                LocalDateTime.of(2005,12,15,0,0),
                "777555333", "FrantaJetel199", null);

        var resultSave = service.createReservation(reservationRequest);

        var result = service.getAllReservationsForPhone(new PhoneSearch("777555333",true));
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(resultSave).isNotNull();
        Assertions.assertThat(result.size()).isEqualTo(0);
    }
}
