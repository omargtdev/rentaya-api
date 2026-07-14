package pe.edu.upc.rentayaapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VisitStatusRequest(
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(Aceptada|Rechazada)$", message = "El estado debe ser Aceptada o Rechazada")
    String status
) {}
