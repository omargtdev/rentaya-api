package pe.edu.upc.rentayaapi.dto;
import pe.edu.upc.rentayaapi.model.Rol;
public record RegisterResponse(Integer id, String firstName, String lastName, String email, String phone, Rol role) {}
