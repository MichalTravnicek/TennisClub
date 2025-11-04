package com.example.tennis.controller;

import com.example.tennis.controller.model.CourtDTO;
import com.example.tennis.controller.model.CreateCourt;
import com.example.tennis.controller.model.UpdateCourt;
import com.example.tennis.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping(CourtController.BASE_URL)
public class CourtController {

    private final CourtService service;

    public static final String BASE_URL = "/api/v1/court";

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "1 - Get",
            summary = "Get all courts",
            description = "Retrieves all courts"
    )
    @ApiResponse(responseCode = "200", description = "Returns list of courts", content = @Content)
    public List<CourtDTO> getAll() {
        //TODO returning all should use paging
        return service.getAllCourts();
    }

    // GET *****************************
    @GetMapping(path = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "1 - Get",
            summary = "Get court by UUID",
            description = "Retrieves a court by unique identifier"
    )
    public ResponseEntity<CourtDTO> getCourt(
            @Parameter(examples = {
                    @ExampleObject(name = "Existing court UUID", description = "Returns court", value = "4930d289-835b-4f60-a326-9bd981389ae6"),
                    @ExampleObject(name = "Nonexistent court UUID", description = "Results in 404 NotFound", value = "b1b44b12-34bc-4ed7-a666-9657b8b8c31b"),
                    @ExampleObject(name = "Invalid UUID", description = "Results in 400 BadRequest", value = "b1b44b12-34bc")
            })
            @RequestParam @org.hibernate.validator.constraints.UUID String uuid) {
        UUID uuidObj = UUID.fromString(uuid);
        CourtDTO dto = new CourtDTO();
        dto.setGlobalId(uuidObj);
        return ResponseEntity.ok(service.getCourt(dto));
    }

    // CREATE *****************************
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "2 - Create",
            summary = "Create court",
            description = "Creates court from supplied values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Court created",
                    content = @Content(schema = @Schema(example = """
                            {
                              "globalId": "aa090dc4-bb65-4fdd-8ff8-00710b5aa41e",
                              "court": "Court 5",
                              "surface": "Gravel"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict with existing court", content = @Content)
    })
    public ResponseEntity<CourtDTO> createCourt(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = CourtDTO.class), examples = {
                    @ExampleObject(name = "Create court", description = "Creates court", value = """
                            {
                              "globalId": "96740538-44ab-47c4-88b1-310e9a8ebd8e",
                              "name": "Court 5",
                              "surface": "Dirt"
                            }
                            """),
                    @ExampleObject(name = "Bad request", description = "Missing name", value = """
                            {
                              "surface": "Dirt"
                            }
                            """),
                    @ExampleObject(name = "Conflicting name", description = "Results in Conflict", value = """
                            {
                              "name": "Court 2",
                              "surface": "Gravel"
                            }
                            """)}))
            @Schema(implementation = CourtDTO.class, example = """
                            {
                              "globalId": "96740538-44ab-47c4-88b1-310e9a8ebd8e",
                              "name": "Court 5",
                              "surface": "Dirt"
                            }
                            """)
            @RequestBody @Validated(CreateCourt.class) CourtDTO request) {
        final CourtDTO createdCourt = service.createCourt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourt);
    }

    // UPDATE *****************************
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(tags = "3 - Update",
            summary = "Update court",
            description = "Updates court from supplied values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Court updated", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request/Not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict with existing court", content = @Content)
    })
    public ResponseEntity<CourtDTO> updateCourt(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = CourtDTO.class), examples = {
                    @ExampleObject(name = "Update name", description = "Updates court", value = """
                            {
                              "globalId": "4930d289-835b-4f60-a326-9bd981389ae6",
                              "name": "Court 9",
                              "surface": "Gravel"
                            }
                            """),
                    @ExampleObject(name = "Missing id", description = "Results in Not found", value = """
                            {
                              "name": "Court 2",
                              "surface": "Gravel"
                            }
                            """),
                    @ExampleObject(name = "Conflicting name", description = "Results in Conflict", value = """
                            {
                              "globalId": "4930d289-835b-4f60-a326-9bd981389ae6",
                              "name": "Court 2",
                              "surface": "Gravel"
                            }
                            """)}))
            @RequestBody @Validated(UpdateCourt.class) CourtDTO court) {
        var update = service.updateCourt(court);
        return ResponseEntity.status(HttpStatus.OK).body(update);
    }

    // DELETE *****************************
    @DeleteMapping("/delete")
    @Operation(tags = "4 - Delete",
            summary = "Delete court",
            description = "Deletes court by id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Court deleted", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Court not found", content = @Content),

    })
    public ResponseEntity<Void> deleteCourt(
            @Parameter(examples = {
                    @ExampleObject(name = "Existing court UUID", description = "Deletes court", value = "4930d289-835b-4f60-a326-9bd981389ae6"),
                    @ExampleObject(name = "Nonexistent court UUID", description = "Results in 404 NotFound", value = "b1b44b12-34bc-4ed7-a666-9657b8b8c31b"),
                    @ExampleObject(name = "Invalid UUID", description = "Results in 400 BadRequest", value = "b1b44b12-34bc")
            }
            )
            @RequestParam @org.hibernate.validator.constraints.UUID String uuid
    ){
        UUID uuidObj = UUID.fromString(uuid);
        var request = new CourtDTO();
        request.setGlobalId(uuidObj);
        service.deleteCourt(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}