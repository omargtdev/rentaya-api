package pe.edu.upc.rentayaapi.dto;

import jakarta.validation.constraints.NotNull;

public record ConversationRequest(
    @NotNull(message = "La propiedad es obligatoria") Integer propertyId
) {}
