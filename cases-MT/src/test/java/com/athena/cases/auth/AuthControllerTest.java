package com.athena.cases.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_returnTokenAndDisplayName_when_credentialsValid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"fm_admin","password":"FM@Admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("fm_admin"))
                .andExpect(jsonPath("$.data.user.displayName").value("ADMKL"))
                .andExpect(jsonPath("$.data.user.roles").isArray())
                .andExpect(jsonPath("$.data.user.permissions").isArray())
                .andExpect(jsonPath("$.data.user.groups").isArray())
                .andExpect(jsonPath("$.data.user.caseTypes").isArray());
    }

    @Test
    void should_returnDisplayNameAndScopes_when_meEndpointCalled() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"fm_admin","password":"FM@Admin123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String body = login.getResponse().getContentAsString();
        String token = body.replaceAll("(?s).*\"accessToken\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("fm_admin"))
                .andExpect(jsonPath("$.data.displayName").value("ADMKL"))
                .andExpect(jsonPath("$.data.groups[0]").value("SYSTEM_ADMINISTRATORS"))
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    @Test
    void should_return401_when_passwordInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"fm_admin","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void should_return400_when_usernameMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"FM@Admin123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return401_when_meCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
