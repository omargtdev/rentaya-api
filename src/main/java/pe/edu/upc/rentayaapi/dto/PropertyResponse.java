package pe.edu.upc.rentayaapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PropertyResponse(
    Integer id,
    Integer ownerId,
    String ownerName,
    String title,
    String district,
    String address,
    BigDecimal price,
    Integer rooms,
    Integer bathrooms,
    Integer area,
    String description,
    List<String> photos,
    String status,
    LocalDate createdAt
) {}
