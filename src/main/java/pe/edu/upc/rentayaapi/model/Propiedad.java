package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
    name = "propiedad",
    indexes = {
        @Index(name = "idx_propiedad_busqueda", columnList = "estado, distrito, precio"),
        @Index(name = "idx_propiedad_propietario", columnList = "propietario_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 50)
    private String distrito;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer habitaciones;

    @Column(nullable = false)
    private Integer banos;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String estado = "Disponible";
}
