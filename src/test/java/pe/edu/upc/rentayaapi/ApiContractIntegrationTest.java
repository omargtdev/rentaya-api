package pe.edu.upc.rentayaapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:contractdb")
class ApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeFrontendFlow() throws Exception {
        register("Owner", "Rentaya", "owner@rentaya.test", "912345678", "PROPIETARIO");
        register("Tenant", "Rentaya", "tenant@rentaya.test", "923456789", "INQUILINO");

        String ownerToken = login("owner@rentaya.test");
        String tenantToken = login("tenant@rentaya.test");

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(tenantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("tenant@rentaya.test"))
            .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(patch("/api/users/me")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "firstName", "Ana",
                    "lastName", "Lopez",
                    "email", "ana@rentaya.test",
                    "phone", "923456789"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Ana"))
            .andExpect(jsonPath("$.role").value("INQUILINO"));

        Map<String, Object> propertyRequest = new LinkedHashMap<>();
        propertyRequest.put("title", "Departamento en Miraflores");
        propertyRequest.put("district", "Miraflores");
        propertyRequest.put("address", "Av. Principal 123");
        propertyRequest.put("price", 2500);
        propertyRequest.put("rooms", 2);
        propertyRequest.put("bathrooms", 1);
        propertyRequest.put("area", 70);
        propertyRequest.put("description", "Propiedad disponible y bien ubicada.");
        propertyRequest.put("photos", List.of("https://example.com/property-1.jpg"));

        mockMvc.perform(post("/api/properties")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(propertyRequest)))
            .andExpect(status().isForbidden());

        MvcResult propertyResult = mockMvc.perform(post("/api/properties")
                .header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(propertyRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ownerName").value("Owner Rentaya"))
            .andExpect(jsonPath("$.status").value("Disponible"))
            .andExpect(jsonPath("$.photos[0]").value("https://example.com/property-1.jpg"))
            .andReturn();
        int propertyId = body(propertyResult).get("id").asInt();

        mockMvc.perform(get("/api/properties")
                .param("district", "Miraflores")
                .param("minPrice", "2000")
                .param("maxPrice", "3000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(propertyId));

        propertyRequest.put("title", "Departamento actualizado");
        mockMvc.perform(put("/api/properties/{id}", propertyId)
                .header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(propertyRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Departamento actualizado"));

        mockMvc.perform(post("/api/favorites/{id}", propertyId)
                .header("Authorization", bearer(tenantToken)))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/favorites/{id}", propertyId)
                .header("Authorization", bearer(tenantToken)))
            .andExpect(status().isConflict());
        mockMvc.perform(get("/api/favorites").header("Authorization", bearer(tenantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(propertyId));

        String visitDate = LocalDate.now().plusDays(1).toString();
        MvcResult visitResult = mockMvc.perform(post("/api/visits")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "propertyId", propertyId,
                    "date", visitDate,
                    "time", "10:00"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tenantName").value("Ana Lopez"))
            .andExpect(jsonPath("$.status").value("Pendiente"))
            .andReturn();
        int visitId = body(visitResult).get("id").asInt();

        mockMvc.perform(post("/api/visits")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("propertyId", propertyId, "date", visitDate, "time", "10:00"))))
            .andExpect(status().isConflict());

        mockMvc.perform(get("/api/visits/owner")
                .header("Authorization", bearer(ownerToken))
                .param("status", "Pendiente"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(visitId));

        mockMvc.perform(patch("/api/visits/{id}/status", visitId)
                .header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "Aceptada"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Aceptada"));

        MvcResult conversationResult = mockMvc.perform(post("/api/conversations")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("propertyId", propertyId))))
            .andExpect(status().isCreated())
            .andReturn();
        String conversationId = body(conversationResult).get("id").asText();

        mockMvc.perform(post("/api/conversations")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("propertyId", propertyId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(conversationId));

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("content", "Hola, ¿sigue disponible?"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.senderName").value("Ana Lopez"));

        mockMvc.perform(get("/api/conversations").header("Authorization", bearer(ownerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].lastMessage").value("Hola, ¿sigue disponible?"));

        mockMvc.perform(get("/api/conversations/{id}/messages", conversationId)
                .header("Authorization", bearer(ownerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].conversationId").value(conversationId));

        mockMvc.perform(delete("/api/favorites/{id}", propertyId)
                .header("Authorization", bearer(tenantToken)))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/properties/{id}", propertyId)
                .header("Authorization", bearer(ownerToken)))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/properties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/catalogs/districts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("Miraflores"));

        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(ownerToken)))
            .andExpect(status().isNoContent());
    }

    @Test
    void frontendValidationErrorsNeverBecomeInternalServerErrors() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "firstName", "Invalid",
                    "lastName", "Role",
                    "email", "invalid-role@rentaya.test",
                    "password", "Password1",
                    "phone", "934567891",
                    "role", "ADMIN"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "firstName", "x".repeat(51),
                    "lastName", "Validation",
                    "email", "long-name@rentaya.test",
                    "password", "Password1",
                    "phone", "934567892",
                    "role", "PROPIETARIO"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.firstName").value("El nombre debe tener máximo 50 caracteres"));

        register("Owner", "Validation", "owner-validation@rentaya.test", "934567893", "PROPIETARIO");
        register("Tenant", "Validation", "tenant-validation@rentaya.test", "934567894", "INQUILINO");
        String ownerToken = login("owner-validation@rentaya.test");
        String tenantToken = login("tenant-validation@rentaya.test");

        Map<String, Object> propertyRequest = new LinkedHashMap<>();
        propertyRequest.put("title", "Departamento para validaciones");
        propertyRequest.put("district", "Lince");
        propertyRequest.put("address", "Av. Validación 100");
        propertyRequest.put("price", 1900);
        propertyRequest.put("rooms", 2);
        propertyRequest.put("bathrooms", 1);
        propertyRequest.put("area", 65);
        propertyRequest.put("description", "Prueba de reglas del frontend.");
        propertyRequest.put("photos", List.of());

        mockMvc.perform(post("/api/properties")
                .header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(propertyRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.photos").isNotEmpty());

        propertyRequest.put("photos", List.of("https://example.com/validation.jpg"));
        MvcResult propertyResult = mockMvc.perform(post("/api/properties")
                .header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(propertyRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        int propertyId = body(propertyResult).get("id").asInt();

        mockMvc.perform(get("/api/properties")
                .param("minPrice", "2000")
                .param("maxPrice", "1000"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());
        mockMvc.perform(get("/api/properties").param("minPrice", "not-a-number"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/visits")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "propertyId", propertyId,
                    "date", LocalDate.now().minusDays(1).toString(),
                    "time", "10:00"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.date").isNotEmpty());
        mockMvc.perform(post("/api/visits")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "propertyId", propertyId,
                    "date", LocalDate.now().plusDays(2).toString(),
                    "time", "12:00"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(patch("/api/visits/{id}/status", 999999)
                .header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "Cancelada"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").isNotEmpty());

        MvcResult conversationResult = mockMvc.perform(post("/api/conversations")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("propertyId", propertyId))))
            .andExpect(status().isCreated())
            .andReturn();
        String conversationId = body(conversationResult).get("id").asText();

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("content", " "))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.content").isNotEmpty());
        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("content", "x".repeat(501)))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.content").isNotEmpty());

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(delete("/api/properties/{id}", propertyId)
                .header("Authorization", bearer(ownerToken)))
            .andExpect(status().isNoContent());
    }

    private void register(String firstName, String lastName, String email, String phone, String role) throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "firstName", firstName,
                    "lastName", lastName,
                    "email", email,
                    "password", "Password1",
                    "phone", phone,
                    "role", role
                ))))
            .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", "Password1"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();
        return body(result).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
