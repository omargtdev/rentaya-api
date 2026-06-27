package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "favorito",
    indexes = {
        @Index(name = "idx_favorito_usuario", columnList = "usuario_id"),
        @Index(name = "idx_favorito_propiedad", columnList = "propiedad_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_favorito_usuario_propiedad", columnNames = {"usuario_id", "propiedad_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;
}
