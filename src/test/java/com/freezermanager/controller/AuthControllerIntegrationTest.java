
package com.freezermanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freezermanager.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret));
    }

    @Test
    void registerCreatesUserAndReturnsJwt() throws Exception {
        String request = """
                {
                "email": "sebas@example.com",
                "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        var user = userRepository.findByEmailIgnoreCase("sebas@example.com")
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertNotEquals(
                "Password123!",
                user.getPassword());

        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches("Password123!", user.getPassword()));
    }

    @Test
    void registerRejectsDuplicateEmailIgnoringCase() throws Exception {
        String request = """
                {
                "email": "sebas@example.com",
                "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated());

        String duplicateRequest = """
                {
                "email": "SEBAS@EXAMPLE.COM",
                "password": "AnotherPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateRequest))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturnsJwtWithCorrectCredentials() throws Exception {
        String registerRequest = """
                {
                "email": "login@example.com",
                "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        String registerRequest = """
                {
                "email": "incorrect-password@example.com",
                "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());

        String incorrectLoginRequest = """
                {
                "email": "incorrect-password@example.com",
                "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incorrectLoginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/storages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAcceptsValidToken() throws Exception {
        String registerRequest = """
                {
                "email": "valid-token@example.com",
                "password": "Password123!"
                }
                """;

        MvcResult registerResult = mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = registerResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        String token = responseJson.get("token").asText();

        mockMvc.perform(
                get("/api/v1/storages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRejectsMalformedToken() throws Exception {
        mockMvc.perform(get("/api/v1/storages")
                .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsTokenWithInvalidSignature() throws Exception {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                new byte[32]);

        String token = Jwts.builder()
                .subject("valid-token@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        mockMvc.perform(get("/api/v1/storages")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsExpiredToken() throws Exception {
        String token = Jwts.builder()
                .subject("valid-token@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(getSigningKey())
                .compact();

        mockMvc.perform(get("/api/v1/storages")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

}
