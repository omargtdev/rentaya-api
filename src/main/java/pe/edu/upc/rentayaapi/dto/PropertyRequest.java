package pe.edu.upc.rentayaapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record PropertyRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    String title,

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 50, message = "El distrito no puede exceder 50 caracteres")
    String district,

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
    String address,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    BigDecimal price,

    @NotNull(message = "Las habitaciones son obligatorias")
    @Positive(message = "Debe haber al menos una habitación")
    Integer rooms,

    @NotNull(message = "Los baños son obligatorios")
    @Positive(message = "Debe haber al menos un baño")
    Integer bathrooms,

    @Positive(message = "El área debe ser mayor a 0")
    Integer area,

    @NotBlank(message = "La descripción es obligatoria")
    String description,

    @NotEmpty(message = "Agrega al menos una foto")
    @Size(max = 8, message = "Se permiten máximo 8 fotos")
    List<
        @NotBlank(message = "La URL de la foto es obligatoria")
        @Size(max = 255, message = "La URL de la foto no puede exceder 255 caracteres") String
    > photos
) {}
