package com.example.tennis.service;

import com.example.tennis.controller.model.CourtDTO;

import java.util.List;

public interface CourtService {

    List<CourtDTO> getAllCourts();

    CourtDTO getCourt(CourtDTO court);

    CourtDTO createCourt(CourtDTO court);

    CourtDTO updateCourt(CourtDTO court);

    void deleteCourt(CourtDTO court);

}
