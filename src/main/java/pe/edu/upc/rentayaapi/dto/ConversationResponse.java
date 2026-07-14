package pe.edu.upc.rentayaapi.dto;

import java.time.Instant;

public record ConversationResponse(
    String id,
    Integer propertyId,
    String propertyTitle,
    Integer ownerId,
    String ownerName,
    Integer tenantId,
    String tenantName,
    String lastMessage,
    Instant lastMessageAt
) {}
