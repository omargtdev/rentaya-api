package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "conversacion",
    indexes = {
        @Index(name = "idx_conversacion_propietario", columnList = "propietario_id, ultimo_mensaje_en"),
        @Index(name = "idx_conversacion_inquilino", columnList = "inquilino_id, ultimo_mensaje_en")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_conversacion_propiedad_participantes",
            columnNames = {"propiedad_id", "propietario_id", "inquilino_id"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Property property;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private User owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private User tenant;

    @Column(name = "ultimo_mensaje", nullable = false, length = 500)
    private String lastMessage = "";

    @Column(name = "ultimo_mensaje_en", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void assignTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastMessageAt == null) {
            lastMessageAt = now;
        }
        if (lastMessage == null) {
            lastMessage = "";
        }
    }
}
