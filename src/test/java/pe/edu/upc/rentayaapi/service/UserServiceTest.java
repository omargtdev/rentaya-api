package pe.edu.upc.rentayaapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.rentayaapi.dto.RegisterRequest;
import pe.edu.upc.rentayaapi.dto.RegisterResponse;
import pe.edu.upc.rentayaapi.exception.EmailAlreadyExistsException;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldSaveUserAndReturnResponse() {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "omar@test.com", "Password1", "987654321", Rol.INQUILINO
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setFirstName(request.firstName());
        savedUser.setLastName(request.lastName());
        savedUser.setEmail(request.email());
        savedUser.setPassword("hashedPassword");
        savedUser.setPhone(request.phone());
        savedUser.setRole(request.role());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = userService.register(request);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.firstName()).isEqualTo("Omar");
        assertThat(response.lastName()).isEqualTo("Gutierrez");
        assertThat(response.email()).isEqualTo("omar@test.com");
        assertThat(response.phone()).isEqualTo("987654321");
        assertThat(response.role()).isEqualTo(Rol.INQUILINO);

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
            "Omar", "Gutierrez", "omar@test.com", "Password1", "987654321", Rol.INQUILINO
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("El correo ya está registrado");

        verify(userRepository).existsByEmail(request.email());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }
}
