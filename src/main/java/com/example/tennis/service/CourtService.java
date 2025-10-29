package com.example.tennis.service;

import com.example.tennis.controller.model.CourtDTO;

public interface CourtService {

    CourtDTO getCourt(CourtDTO court);

    CourtDTO createCourt(CourtDTO court);

    CourtDTO updateCourt(CourtDTO court);

    void deleteCourt(CourtDTO court);

}
