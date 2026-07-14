package pe.edu.upc.rentayaapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record VisitResponse(
    Integer id,
    Integer propertyId,
    String propertyTitle,
    Integer tenantId,
    String tenantName,
    Integer ownerId,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime time,
    String status,
    Instant createdAt
) {}
