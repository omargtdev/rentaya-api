package pe.edu.upc.rentayaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "mensaje",
    indexes = {
        @Index(name = "idx_mensaje_conversacion", columnList = "propiedad_id, emisor_id, receptor_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Property property;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private User sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receptor_id", nullable = false)
    private User receiver;

    @Column(name = "contenido", nullable = false, length = 500)
    private String content;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime sentAt;
}
