# Plan: Entity Refactor to English

## Branch
`feat/og/entity.refactor` (created from `main`, in `rentaya-api` only)

## Goal
Rename all JPA entity class names and field names from Spanish to English. Keep all `@Table`, `@Column(name=...)`, and `@JoinColumn(name=...)` values in Spanish so the database schema (`schema.sql`) is NOT modified.

## Context
- Repo: `/home/linuxero/repos/upc/rentaya-api`
- Branch already created and checked out: `feat/og/entity.refactor`
- 6 entity files to refactor, all in `src/main/java/pe/edu/upc/rentayaapi/model/`

## Files to DELETE (old Spanish class names)
- `src/main/java/pe/edu/upc/rentayaapi/model/Usuario.java`
- `src/main/java/pe/edu/upc/rentayaapi/model/Propiedad.java`
- `src/main/java/pe/edu/upc/rentayaapi/model/Foto.java`
- `src/main/java/pe/edu/upc/rentayaapi/model/Visita.java`
- `src/main/java/pe/edu/upc/rentayaapi/model/Mensaje.java`
- `src/main/java/pe/edu/upc/rentayaapi/model/Favorito.java`

## Files to CREATE (new English class names)

### User.java (replaces Usuario.java)
```java
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
```

### Property.java (replaces Propiedad.java)
```java
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

    @Column(name = "descripcion", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "estado", nullable = false, length = 20)
    private String status = "Disponible";
}
```

### Photo.java (replaces Foto.java)
```java
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
```

### Visit.java (replaces Visita.java)
```java
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
```

### Message.java (replaces Mensaje.java)
```java
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
```

### Favorite.java (replaces Favorito.java)
```java
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
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Property property;
}
```

## Field Renaming Summary
| Entity (old → new) | Field (old → new) | `@Column`/`@JoinColumn` (unchanged) |
|---|---|---|
| Usuario → User | nombre → firstName | nombre |
| | apellido → lastName | apellido |
| | correo → email | correo |
| | contrasena → password | contrasena |
| | telefono → phone | telefono |
| | rol → role | rol |
| Propiedad → Property | propietario → owner | propietario_id |
| | titulo → title | titulo |
| | distrito → district | distrito |
| | direccion → address | direccion |
| | precio → price | precio |
| | habitaciones → bedrooms | habitaciones |
| | banos → bathrooms | banos |
| | descripcion → description | descripcion |
| | estado → status | estado |
| Foto → Photo | propiedad → property | propiedad_id |
| Visita → Visit | propiedad → property | propiedad_id |
| | inquilino → tenant | inquilino_id |
| | fecha → date | fecha |
| | hora → time | hora |
| | estado → status | estado |
| Mensaje → Message | propiedad → property | propiedad_id |
| | emisor → sender | emisor_id |
| | receptor → receiver | receptor_id |
| | contenido → content | contenido |
| | fecha_envio → sentAt | fecha_envio |
| Favorito → Favorite | usuario → user | usuario_id |
| | propiedad → property | propiedad_id |

## Verification
- Run `./mvnw clean compile` to verify compilation succeeds
- Run `./mvnw clean test` to verify tests pass
- The `StatusController` and `StatusService` do NOT reference any entity, so they should not need changes

## Commit
- Single commit: "Refactor entity class and field names to English"
- Push to origin: `git push -u origin feat/og/entity.refactor`
- Create a PR from `feat/og/entity.refactor` to `main` (since main is protected)