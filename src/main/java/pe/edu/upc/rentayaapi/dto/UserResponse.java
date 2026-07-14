package pe.edu.upc.rentayaapi.dto;

import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.User;

public record UserResponse(
    Integer id,
    String firstName,
    String lastName,
    String email,
    String phone,
    Rol role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole()
        );
    }
}
