package pe.edu.upc.rentayaapi.dto;

import jakarta.validation.constraints.*;
import pe.edu.upc.rentayaapi.model.Rol;

public record RegisterRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre debe tener máximo 50 caracteres")
    String firstName,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido debe tener máximo 50 caracteres")
    String lastName,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido")
    @Size(max = 100, message = "El correo debe tener máximo 100 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Mínimo 8 caracteres, 1 mayúscula y 1 número"
    )
    String password,

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{9}$", message = "Ingresa un teléfono válido")
    String phone,

    @NotNull(message = "El rol es obligatorio")
    Rol role
) {}
