package com.example.tennis.controller;

import com.example.tennis.controller.security.JwtInterceptor;
import com.example.tennis.security.AuthenticationConfig;
import com.example.tennis.security.SecurityConfig;
import com.example.tennis.service.CourtService;
import com.example.tennis.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(printOnlyOnFailure = false)
@WebMvcTest(controllers = {ReservationController.class, CourtController.class, AuthController.class})
@Import({AuthenticationConfig.class, SecurityConfig.class, JwtService.class, JwtInterceptor.class,
})
//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SecurityControllerTest {

    String userToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc2M" +
            "jYzMjQ2OSwiZXhwIjo1MzYyNjMyNDY5fQ.eSLS9MZqbXgw5mtgwdi6JZB8qI1YrFjtgl9bPS74KqI";

    String adminToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxN" +
            "zYyNjI3Nzc4LCJleHAiOjUzNjI2Mjc3Nzh9.Rf9nmAYQlHwVvRB4PsUSFCqyW9-z00J3Uc_G-XL90lw";

    String unknownToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1bmtub3duIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI" +
            "6MTc2MjYzMjM5MCwiZXhwIjo1MzYyNjMyMzkwfQ.FV2RYk86phxKPpCrceYxztaODxdA02zI7Mjy4wN-yds";

    String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc2M" +
            "jMyNzMxMSwiZXhwIjoxNzYyMzMwOTExfQ.ysuhk8lUazZ_5fKrZdrrpdAXBeW294yM1w36-QVpB4E";


    @MockitoBean
    ReservationService reservationService;

    @MockitoBean
    CourtService courtService;

    @Autowired
    private MockMvc mockMvc;

    private MockHttpServletRequestBuilder tokenGet(String uri, String token){
        return get(uri).header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder tokenPost(String uri, String token){
        return post(uri).header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder tokenPut(String uri, String token){
        return put(uri).header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder tokenDelete(String uri, String token){
        return delete(uri).header("Authorization", "Bearer " + token);
    }

    @Test
    public void unauthenticatedEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Authorization","Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(status().isOk());
    }

    @Test
    public void whenUserHasRole_thenAccessIsAllowed() throws Exception {
        mockMvc.perform(tokenGet(ReservationController.BASE_URL+ "/", userToken))
                        .andExpect(status().isOk());
        mockMvc.perform(tokenGet(ReservationController.BASE_URL +"/get", userToken)
                        .param("uuid","2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"))
                .andExpect(status().isOk());
    }

    @Test
    public void whenMissingToken_thenAccessIsForbidden() throws Exception {
        mockMvc.perform(tokenGet(ReservationController.BASE_URL +"/get", null)
                        .param("uuid","2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenMissingToken_thenAccessIsForbidden2() throws Exception {
        mockMvc.perform(get(ReservationController.BASE_URL +"/get")
                        .header("Authorization","")
                        .param("uuid","2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenAuthorizationMismatch_thenAccessIsForbidden() throws Exception {
        mockMvc.perform(tokenGet(ReservationController.BASE_URL +"/get", unknownToken)
                        .param("uuid","2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenAuthorizationMismatch_thenAccessIsForbidden2() throws Exception {
        mockMvc.perform(tokenGet(ReservationController.BASE_URL +"/",unknownToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(tokenGet(CourtController.BASE_URL +"/",unknownToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUserDoesNotHaveRole_thenAccessIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reservation/"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/v1/court/"))
                .andExpect(status().is4xxClientError());
    }

    //TODO method security is invoked after validations
    @Test
    void whenUserTriesUpdateOrDelete_thenAccessIsForbidden() throws Exception {
        var updateRequest = """
                            {
                              "globalId" : "b9376343-e083-4aa7-8af6-1d22693646de",
                              "court": "Court 1"
                            }
                            """;
        mockMvc.perform(tokenPut(CourtController.BASE_URL + "/update", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isForbidden());
        mockMvc.perform(tokenPut(ReservationController.BASE_URL + "/update", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isForbidden());
        mockMvc.perform(tokenDelete(ReservationController.BASE_URL + "/delete", userToken)
                        .param("uuid","3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(status().isForbidden());
        mockMvc.perform(tokenDelete(CourtController.BASE_URL + "/delete", userToken)
                        .param("uuid","3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUserTriesCreate_thenAccessIsForbidden() throws Exception {
        var updateRequest = """
                {
                  "globalId" : "b9376343-e083-4aa7-8af6-1d22693646de",
                  "name": "Court 11",
                  "surface": "Gravel"
                }
                """;
        mockMvc.perform(tokenPost(CourtController.BASE_URL + "/create", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenAdminTriesCreate_thenAccessIsAllowed() throws Exception {
        var updateRequest = """
                {
                  "globalId" : "b9376343-e083-4aa7-8af6-1d22693646de",
                  "name": "Court 11",
                  "surface": "Gravel"
                }
                """;
        mockMvc.perform(tokenPost(CourtController.BASE_URL + "/create", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void whenAdminTriesUpdateOrDelete_thenAccessIsAllowed() throws Exception {
        var updateRequest = """
                            {
                              "globalId" : "b9376343-e083-4aa7-8af6-1d22693646de",
                              "court": "Court 1",
                              "gameType": "Singles",
                              "phone": "777321987",
                              "customer": "Pavel Prochazka"
                            }
                            """;
        mockMvc.perform(tokenPut("/api/v1/reservation/update", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(tokenPut("/api/v1/reservation/update", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(tokenDelete(ReservationController.BASE_URL + "/delete", adminToken)
                        .param("uuid","3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(tokenDelete(CourtController.BASE_URL + "/delete", adminToken)
                        .param("uuid","4930d289-835b-4f60-a326-9bd981389ae6"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testExpiredToken() throws Exception {
        mockMvc.perform(tokenGet(ReservationController.BASE_URL +"/get", expiredToken)
                        .param("uuid","2b8ca6b3-126e-4a4b-a6cc-41fccc00ce11"))
                .andExpect(status().isUnauthorized());
    }
}
