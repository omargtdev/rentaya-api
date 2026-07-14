package pe.edu.upc.rentayaapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upc.rentayaapi.dto.RegisterRequest;
import pe.edu.upc.rentayaapi.dto.RegisterResponse;
import pe.edu.upc.rentayaapi.exception.EmailAlreadyExistsException;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.service.UserProfileService;
import pe.edu.upc.rentayaapi.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserProfileService userProfileService;

    @Test
    void register_shouldReturn201WhenValid() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "omar@test.com", "Password1", "987654321", Rol.INQUILINO
        );
        RegisterResponse response = new RegisterResponse(
            1, "Omar", "Gutierrez", "omar@test.com", "987654321", Rol.INQUILINO
        );

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Omar"))
            .andExpect(jsonPath("$.lastName").value("Gutierrez"))
            .andExpect(jsonPath("$.email").value("omar@test.com"))
            .andExpect(jsonPath("$.phone").value("987654321"))
            .andExpect(jsonPath("$.role").value("INQUILINO"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_shouldReturn400WhenInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "invalid-email", "Password1", "987654321", Rol.INQUILINO
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "omar@test.com", "weak", "987654321", Rol.INQUILINO
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409WhenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "omar@test.com", "Password1", "987654321", Rol.INQUILINO
        );

        when(userService.register(any(RegisterRequest.class)))
            .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.email").value("El correo ya está registrado"));
    }

    @Test
    void register_shouldReturn400WhenInvalidPhone() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "omar@test.com", "Password1", "123", Rol.INQUILINO
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
