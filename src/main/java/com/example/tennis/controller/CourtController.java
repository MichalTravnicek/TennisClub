package com.example.tennis.controller;

import com.example.tennis.controller.model.CourtDTO;
import com.example.tennis.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(CourtController.BASE_URL)
public class CourtController {

    private final CourtService courtService;

    public static final String BASE_URL = "/api/v1/court";

    @GetMapping("/")
    @Operation(tags = "1 - Get",
            summary = "Get all courts",
            description = "Retrieves all courts"
    )
    @ApiResponse(responseCode = "200", description = "Returns list of courts", content = @Content)
    public List<CourtDTO> getAll() {
        //TODO returning all should use paging
        return courtService.getAllCourts();
    }

}
