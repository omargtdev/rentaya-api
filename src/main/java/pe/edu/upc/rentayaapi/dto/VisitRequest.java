package pe.edu.upc.rentayaapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record VisitRequest(
    @NotNull(message = "La propiedad es obligatoria")
    Integer propertyId,

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "No se pueden seleccionar fechas anteriores al día actual.")
    LocalDate date,

    @NotNull(message = "La hora es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    LocalTime time
) {}
