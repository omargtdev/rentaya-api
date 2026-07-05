package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "foto",
    indexes = {
        @Index(name = "idx_foto_propiedad", columnList = "propiedad_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Property property;

    @Column(nullable = false, length = 255)
    private String url;
}
