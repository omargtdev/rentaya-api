package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String firstName;

    @Column(name = "apellido", nullable = false, length = 50)
    private String lastName;

    @Column(name = "correo", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String password;

    @Column(name = "telefono", nullable = false, length = 15)
    private String phone;

    @Column(name = "rol", nullable = false, length = 20)
    private String role;
}
