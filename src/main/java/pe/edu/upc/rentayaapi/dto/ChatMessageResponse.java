package pe.edu.upc.rentayaapi.dto;

import java.time.Instant;

public record ChatMessageResponse(
    Integer id,
    String conversationId,
    Integer senderId,
    String senderName,
    String content,
    Instant sentAt
) {}
