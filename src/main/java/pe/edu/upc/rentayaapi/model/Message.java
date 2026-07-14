package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "mensaje",
    indexes = {
        @Index(name = "idx_mensaje_conversacion", columnList = "conversacion_id, fecha_envio")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "emisor_id", nullable = false)
    private User sender;

    @Column(name = "contenido", nullable = false, length = 500)
    private String content;

    @Column(name = "fecha_envio", nullable = false)
    private java.time.Instant sentAt;

    @PrePersist
    void assignSentAt() {
        if (sentAt == null) {
            sentAt = java.time.Instant.now();
        }
    }
}
