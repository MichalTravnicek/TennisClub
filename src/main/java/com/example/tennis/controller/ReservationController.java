package com.example.tennis.controller;

import com.example.tennis.controller.model.ReservationDTO;
import com.example.tennis.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping(ReservationController.BASE_URL)
public class ReservationController {

    private final ReservationService service;

    public static final String BASE_URL = "/api/v1/reservation";

    @GetMapping("/")
    @Operation(tags = "1 - Get",
            summary = "Get all reservations",
            description = "Retrieves all reservations"
    )
    @ApiResponse(responseCode = "200", description = "Returns list of reservations", content = @Content)
    public List<ReservationDTO> getAll() {
        //TODO returning all should use paging
        return service.getAllReservations();
    }

    @GetMapping("/get")
    @Operation(tags = "1 - Get",
            summary = "Get reservation by UUID",
            description = "Retrieves a reservation by unique identifier"
    )
    public ResponseEntity<ReservationDTO> getReservation(
            @Parameter(examples = {
                    @ExampleObject(name = "Existing reservation UUID", description = "Returns reservation", value = "2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"),
                    @ExampleObject(name = "Nonexistent reservation UUID", description = "Results in 404 NotFound", value = "b1b44b12-34bc-4ed7-a666-9657b8b8c31b"),
                    @ExampleObject(name = "Invalid UUID", description = "Results in 400 BadRequest", value = "b1b44b12-34bc")
            })
            @RequestParam @org.hibernate.validator.constraints.UUID String uuid) {
        UUID uuidObj = UUID.fromString(uuid);
        ReservationDTO dto = new ReservationDTO();
        dto.setGlobalId(uuidObj);
        return ResponseEntity.ok(service.getReservation(dto));
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "2 - Create",
            summary = "Create reservation",
            description = "Creates reservation from supplied values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created",
                    content = @Content(schema = @Schema(example = """
                            {
                              "globalId": "aa090dc4-bb65-4fdd-8ff8-00710b5aa41e",
                              "court": "Court 1",
                              "gameType": "Singles",
                              "startTime": "2029-09-25 00:00:00",
                              "endTime": "2029-09-26 00:00:00",
                              "phone": "777321987",
                              "customer": "Pavel Prochazka"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict with existing reservation", content = @Content)
    })
    public ResponseEntity<ReservationDTO> createReservation(
            @Schema(implementation = ReservationDTO.class, example = """
                            {
                              "globalId": "aa090dc4-bb65-4fdd-8ff8-00710b5aa41e",
                              "court": "Court 1",
                              "gameType": "Singles",
                              "startTime": "2029-09-25 00:00:00",
                              "endTime": "2029-09-26 00:00:00",
                              "phone": "777321987",
                              "customer": "Pavel Prochazka"
                            }
                            """)
            @RequestBody ReservationDTO request) {
        final ReservationDTO createdReservation = service.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "3 - Update",
            summary = "Update reservation",
            description = "Updates reservation from supplied values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation updated", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request/Not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict with existing reservation", content = @Content)
    })
    public ResponseEntity<ReservationDTO> updateReservation(
            @Schema(implementation = ReservationDTO.class, example = """
                            {
                              "globalId": "b423ccb2-0fb7-4df4-b510-55c89f682214",
                              "court": "Court 2",
                              "gameType": "Doubles",
                              "startTime": "2039-10-25 00:00:00",
                              "endTime": "2039-10-26 00:00:00",
                              "phone": "777321987",
                              "customer": "Pavel Prochazka"
                            }
                            """)
            @RequestBody @Valid ReservationDTO reservation) {
        service.updateReservation(reservation);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(tags = "4 - Delete",
            summary = "Delete reservation",
            description = "Deletes reservation by id"
    )
    @DeleteMapping("/delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content),

    })
    public ResponseEntity<Void> deleteReservation(
            @Parameter(examples = {
                    @ExampleObject(name = "Existing reservation UUID", description = "Deletes reservation", value = "54027dc9-a61d-4ce1-8616-7397595d00e3"),
                    @ExampleObject(name = "Nonexistent reservation UUID", description = "Results in 404 NotFound", value = "b1b44b12-34bc-4ed7-a666-9657b8b8c31b"),
                    @ExampleObject(name = "Invalid UUID", description = "Results in 400 BadRequest", value = "b1b44b12-34bc")
            }
            )
            @RequestParam @org.hibernate.validator.constraints.UUID String uuid
    ){
        UUID uuidObj = UUID.fromString(uuid);
        var request = new ReservationDTO();
        request.setGlobalId(uuidObj);
        service.deleteReservation(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
