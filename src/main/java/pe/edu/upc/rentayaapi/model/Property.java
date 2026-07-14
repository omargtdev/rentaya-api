package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private User owner;

    @Column(name = "titulo", nullable = false, length = 100)
    private String title;

    @Column(name = "distrito", nullable = false, length = 50)
    private String district;

    @Column(name = "direccion", nullable = false, length = 150)
    private String address;

    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "habitaciones", nullable = false)
    private Integer bedrooms;

    @Column(name = "banos", nullable = false)
    private Integer bathrooms;

    @Column(name = "area")
    private Integer area;

    @Column(name = "descripcion", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "estado", nullable = false, length = 20)
    private String status = "Disponible";

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDate createdAt;

    @PrePersist
    void assignCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
}
