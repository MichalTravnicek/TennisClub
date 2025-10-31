package com.example.tennis.controller;

import com.example.tennis.controller.model.ReservationDTO;
import com.example.tennis.service.ReservationService;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService service;

    @InjectMocks
    private ReservationController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES,false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getAllReservations_ShouldReturnListOfReservations() throws Exception {
        ReservationDTO dto1 = new ReservationDTO();
        dto1.setGlobalId(UUID.randomUUID());
        ReservationDTO dto2 = new ReservationDTO();
        dto2.setGlobalId(UUID.randomUUID());
        List<ReservationDTO> allReservations = Arrays.asList(dto1, dto2);

        when(service.getAllReservations()).thenReturn(allReservations);

        mockMvc.perform(get(ReservationController.BASE_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(allReservations)));
    }

    @Test
    void getReservationByUuid_WhenFound_ShouldReturnReservation() throws Exception {
        UUID uuid = UUID.randomUUID();
        ReservationDTO dto = new ReservationDTO();
        dto.setGlobalId(uuid);

        when(service.getReservation(any(ReservationDTO.class))).thenReturn(dto);

        mockMvc.perform(get(ReservationController.BASE_URL + "/get")
                        .param("uuid", uuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    void createReservation_ShouldReturnCreatedReservationAndStatusCreated() throws Exception {
        ReservationDTO requestDto = new ReservationDTO();
        requestDto.setCustomer("Pavel Prochazka");
        ReservationDTO createdDto = new ReservationDTO();
        createdDto.setGlobalId(UUID.randomUUID());
        createdDto.setCustomer("Pavel Prochazka");

        when(service.createReservation(any(ReservationDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post(ReservationController.BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(createdDto)));
    }

    @Test
    void updateReservation_WhenSuccessful_ShouldReturnStatusNoContent() throws Exception {
        ReservationDTO requestDto = new ReservationDTO();
        requestDto.setGlobalId(UUID.randomUUID());
        requestDto.setCustomer("Pavel Prochazka");
        requestDto.setCourt("Court 1");
        requestDto.setGameType("Singles");
        requestDto.setPhone("777123456");
        requestDto.setStartTime(LocalDateTime.of(2050, 5, 5, 0, 0));
        requestDto.setEndTime(LocalDateTime.of(2050, 5, 10, 0, 0));

        when(service.updateReservation(any(ReservationDTO.class))).thenReturn(requestDto);

        System.err.println(objectMapper.writeValueAsString(requestDto));
        mockMvc.perform(put(ReservationController.BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateReservation_WhenNotFound_ShouldReturnStatusBadRequest() throws Exception {
        ReservationDTO requestDto = new ReservationDTO();
        requestDto.setGlobalId(UUID.randomUUID());

        mockMvc.perform(put(ReservationController.BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReservation_ShouldReturnStatusNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        doNothing().when(service).deleteReservation(any(ReservationDTO.class));

        mockMvc.perform(delete(ReservationController.BASE_URL + "/delete")
                        .param("uuid", uuid.toString()))
                .andExpect(status().isNoContent());
    }
}
