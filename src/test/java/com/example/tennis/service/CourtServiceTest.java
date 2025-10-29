package com.example.tennis.service;

import com.example.tennis.controller.model.CourtDTO;
import com.example.tennis.persistence.TestTool;
import com.example.tennis.persistence.entity.Court;
import com.example.tennis.persistence.entity.GameType;
import com.example.tennis.persistence.entity.Reservation;
import com.example.tennis.persistence.entity.Surface;
import com.example.tennis.persistence.exception.NotFoundException;
import com.example.tennis.persistence.repository.SearchRepository;
import com.example.tennis.persistence.repository.TennisDAO;
import com.example.tennis.persistence.repository.TennisRepository;
import com.example.tennis.service.exception.ConflictException;
import com.example.tennis.service.impl.CourtServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import({TennisRepository.class,
        SearchRepository.class,
        CourtServiceImpl.class,
})
@DataJpaTest
class CourtServiceTest {

    @Autowired
    private TennisDAO repository;

    @Autowired
    private CourtService service;

    @Test
    public void getCourt() {
        Court court = TestTool.createCourt("ServiceCourt1","DirtXXX",100L);
        repository.save(court);
        CourtDTO dto = new CourtDTO(court.getGlobalId(),null,null);
        var result = service.getCourt(dto);
        System.err.println(result);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getGlobalId()).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("ServiceCourt1");
        Assertions.assertThat(result.getSurface()).isEqualTo("DirtXXX");
    }

    @Test
    public void getCourtByName() {
        Court court = TestTool.createCourt("ServiceCourt1","DirtXXX",100L);
        repository.save(court);
        CourtDTO dto = new CourtDTO(null,"ServiceCourt1",null);
        var result = service.getCourt(dto);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getGlobalId()).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("ServiceCourt1");
        Assertions.assertThat(result.getSurface()).isEqualTo("DirtXXX");
    }

    @Test
    public void getCourtNoResult() {
        CourtDTO dto = new CourtDTO(null,null,null);
        assertThrows(NotFoundException.class,
                () -> service.getCourt(dto)
        );
    }

    @Test
    public void getCourtFail() {
        CourtDTO dto = new CourtDTO(null,"unknown",null);
        assertThrows(
                NotFoundException.class,
                () -> service.getCourt(dto)
        );
    }

    @Test
    public void createCourt() {
        Surface surface = new Surface("Bahno",50L);
        repository.save(surface);
        CourtDTO dto = new CourtDTO(null, "ServiceCourt2","Bahno");
        var result = service.createCourt(dto);
        System.err.println(result);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getGlobalId()).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("ServiceCourt2");
        Assertions.assertThat(result.getSurface()).isEqualTo("Bahno");
    }

    @Test
    public void createCourtAlreadyExists() {
        Surface surface = new Surface("Hlina",50L);
        repository.save(surface);
        CourtDTO dto = new CourtDTO(null, "ServiceCourt4","Hlina");
        service.createCourt(dto);
        assertThrows(
                ConflictException.class,
                ()-> service.createCourt(dto)
        );
    }

    @Test
    public void createCourtSurfaceNotFound() {
        CourtDTO dto = new CourtDTO(null, "ServiceCourt5","Pisek");
        assertThrows(
                NotFoundException.class,
                ()-> service.createCourt(dto)
        );
    }

    @Test
    public void updateCourt() {
        Surface surface = new Surface("Bahno2",50L);
        repository.save(surface);

        CourtDTO dto = new CourtDTO(null, "ServiceCourt23", "Bahno2");
        var result = service.createCourt(dto);

        CourtDTO dto2 = new CourtDTO(result.getGlobalId(),"ServiceCourt6","");
        var updated = service.updateCourt(dto2);

        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getName()).isEqualTo("ServiceCourt6");
        System.err.println(updated);
    }

    @Test
    public void updateCourtExistingName() {
        Surface surface = new Surface("Bahno2",50L);
        repository.save(surface);
        Surface surface2 = new Surface("Bahno22",60L);
        repository.save(surface2);

        CourtDTO dto = new CourtDTO(null, "ServiceCourt32", "Bahno2");
        var result = service.createCourt(dto);
        CourtDTO dto2 = new CourtDTO(null, "ServiceCourt35", "Bahno22");
        var result2 = service.createCourt(dto2);

        CourtDTO dto3 = new CourtDTO(result.getGlobalId(),"ServiceCourt32","");

        assertThrows(
                ConflictException.class,
                () -> service.updateCourt(dto3)
        );
    }

    @Test
    public void createCourtSameSurface() {
        Surface surface2 = new Surface("Bahno33",60L);
        repository.save(surface2);

        CourtDTO dto = new CourtDTO(null, "ServiceCourt42", "Bahno33");
        var result = service.createCourt(dto);
        CourtDTO dto2 = new CourtDTO(null, "ServiceCourt45", "Bahno33");
        var result2 = service.createCourt(dto2);
    }

    @Test
    public void updateCourtSurface() {
        repository.save(new Surface("Bahno3",50L));
        repository.save(new Surface("Bahno4",150L));

        CourtDTO dto = new CourtDTO(null, "ServiceCourt22","Bahno3");
        var result = service.createCourt(dto);

        CourtDTO dto2 = new CourtDTO(result.getGlobalId(),"ServiceCourt7","Bahno4");
        var updated = service.updateCourt(dto2);

        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getName()).isEqualTo("ServiceCourt7");
        Assertions.assertThat(updated.getSurface()).isEqualTo("Bahno4");
        System.err.println(updated);
    }

    @Test
    public void updateCourtNotFound() {
        CourtDTO dto = new CourtDTO(null, "ServiceCourt8","Pisek");
        assertThrows(
                NotFoundException.class,
                ()-> service.updateCourt(dto)
        );
    }

    @Test
    public void updateCourtSurfaceNotFound() {
        Surface surface = new Surface("Bahno5",50L);
        repository.save(surface);
        CourtDTO dto = new CourtDTO(null, "ServiceCourt25","Bahno5");
        var result = service.createCourt(dto);
        CourtDTO dto2 = new CourtDTO(null, "ServiceCourt25","Pisek4");
        assertThrows(
                NotFoundException.class,
                ()-> service.updateCourt(dto2)
        );
    }

    @Test
    public void deleteCourt() {
        Surface surface = new Surface("Bahno5",50L);
        repository.save(surface);
        CourtDTO dto = new CourtDTO(null, "ServiceCourt25","Bahno5");
        service.createCourt(dto);
        service.deleteCourt(dto);
    }

    @Test
    public void deleteCourtNotFound() {
        CourtDTO dto = new CourtDTO(null, "ServiceCourt25","Bahno5");
        assertThrows(
                NotFoundException.class,
                () -> service.deleteCourt(dto)
        );
    }

    @Test
    public void deleteCourtWithReservations() {
        Court court = TestTool.createCourt("ServiceCourt29","Bazina", 120L);
        repository.save(court);
        GameType gameType = repository.getAll(GameType.class).getFirst();
        Reservation reservation = TestTool.createReservation(court,"MichalT","777120120");
        reservation.setGameType(gameType);
        repository.save(reservation);
        CourtDTO dto = new CourtDTO(null, "ServiceCourt29","Bazina");
        assertThrows(
                ConflictException.class,
                () -> service.deleteCourt(dto)
        );
    }
}
