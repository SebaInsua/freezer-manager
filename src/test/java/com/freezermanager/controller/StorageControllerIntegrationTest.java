package com.freezermanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.user.User;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    StorageRepository storageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testIntegrationWhenStoragesItsEmpty() throws Exception {
        String email = "empty@example.com";
        createUser(email);
        String token = createToken(email);

        mockMvc.perform(get("/api/v1/storages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void testIntegrationWhenExistsStorages() throws Exception {
        String email = "storages@example.com";
        createUser(email);

        createStorage(email, "Philco", BigDecimal.valueOf(150));
        createStorage(email, "Samsung", BigDecimal.valueOf(250));

        String token = createToken(email);

        mockMvc.perform(get("/api/v1/storages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].capacidadLitros",
                        comparesEqualTo(150)))
                .andExpect(jsonPath("$.content[1].capacidadLitros",
                        comparesEqualTo(250)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testIntegrationStorageById() throws Exception {
        String email = "storage-id@example.com";
        createUser(email);

        Storage storage = createStorage(
                email, "Philco", BigDecimal.valueOf(120));

        String token = createToken(email);

        mockMvc.perform(get("/api/v1/storages/{id}", storage.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storage.getId()))
                .andExpect(jsonPath("$.marca").value("Philco"));
    }

    @Test
    void testIntegrationWhenStorageDoesntExist() throws Exception {
        String email = "missing-storage@example.com";
        createUser(email);

        Storage storage = createStorage(
                email, "Philco", BigDecimal.valueOf(120));

        Long idInexistente = storage.getId() + 1;

        while (storageRepository.existsById(idInexistente)) {
            idInexistente++;
        }

        String token = createToken(email);
        String messageExpected =
                "No existe un Freezer con el ID: " + idInexistente;

        mockMvc.perform(get("/api/v1/storages/{id}", idInexistente)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testIntegrationStoragesWithPagination() throws Exception {
        String email = "pagination@example.com";
        createUser(email);

        for (int i = 1; i <= 5; i++) {
            createStorage(email, "Marca" + i, BigDecimal.valueOf(i * 100));
        }

        String token = createToken(email);

        mockMvc.perform(get("/api/v1/storages")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].marca").value("Marca1"))
                .andExpect(jsonPath("$.content[1].marca").value("Marca2"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void testIntegrationStoragesWithSecondPage() throws Exception {
        String email = "pagination-second@example.com";
        createUser(email);

        for (int i = 1; i <= 5; i++) {
            createStorage(email, "Marca" + i, BigDecimal.valueOf(i * 100));
        }

        String token = createToken(email);

        mockMvc.perform(get("/api/v1/storages")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].marca").value("Marca3"))
                .andExpect(jsonPath("$.content[1].marca").value("Marca4"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void testIntegrationStoragesWithDescendingSort() throws Exception {
        String email = "sorting@example.com";
        createUser(email);

        createStorage(email, "Philco", BigDecimal.valueOf(150));
        createStorage(email, "Samsung", BigDecimal.valueOf(250));
        createStorage(email, "Gafa", BigDecimal.valueOf(200));

        String token = createToken(email);

        mockMvc.perform(get("/api/v1/storages")
                        .header("Authorization", "Bearer " + token)
                        .param("sort", "marca,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].marca").value("Samsung"))
                .andExpect(jsonPath("$.content[1].marca").value("Philco"))
                .andExpect(jsonPath("$.content[2].marca").value("Gafa"));
    }

    @Test
    void testCreateStorageWithAuthenticatedUser() throws Exception {
        String email = "create-storage@example.com";
        createUser(email);

        String token = createToken(email);

        String request = """
                {
                    "marca": "Philco",
                    "modelo": "Vertical",
                    "capacidadLitros": 250
                }
                """;

        mockMvc.perform(post("/api/v1/storages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.marca").value("Philco"))
                .andExpect(jsonPath("$.modelo").value("Vertical"))
                .andExpect(jsonPath("$.capacidadLitros").value(250));
    }

    @Test
    void testCreateStorageWithoutTokenIsUnauthorized() throws Exception {
        String request = """
                {
                    "marca": "Philco",
                    "modelo": "Vertical",
                    "capacidadLitros": 250
                }
                """;

        mockMvc.perform(post("/api/v1/storages")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUserCannotAccessAnotherUsersStorage() throws Exception {
        String ownerEmail = "owner-storage@example.com";
        String otherEmail = "other-storage@example.com";

        createUser(ownerEmail);
        createUser(otherEmail);

        Storage storage = createStorage(
                ownerEmail, "Philco", BigDecimal.valueOf(250));

        String otherToken = createToken(otherEmail);

        mockMvc.perform(get("/api/v1/storages/{id}", storage.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-test-password");
        return userRepository.save(user);
    }

    private String createToken(String email) {
        return jwtService.generateToken(email);
    }

    private Storage createStorage(String email, String marca, BigDecimal capacidad) {
        User owner = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> createUser(email));

        Storage storage = new Storage();
        storage.setOwner(owner);
        storage.setMarca(marca);
        storage.setCapacidadLitros(capacidad);

        return storageRepository.save(storage);
    }

}
