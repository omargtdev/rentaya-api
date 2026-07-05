package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "visita",
    indexes = {
        @Index(name = "idx_visita_propiedad_estado", columnList = "propiedad_id, estado"),
        @Index(name = "idx_visita_inquilino", columnList = "inquilino_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Property property;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private User tenant;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false, length = 20)
    private String status = "Pendiente";
}
