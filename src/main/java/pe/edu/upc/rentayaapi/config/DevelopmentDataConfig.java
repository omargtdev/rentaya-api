package pe.edu.upc.rentayaapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.Photo;
import pe.edu.upc.rentayaapi.model.Property;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.PhotoRepository;
import pe.edu.upc.rentayaapi.repository.PropertyRepository;
import pe.edu.upc.rentayaapi.repository.UserRepository;

import java.math.BigDecimal;

@Configuration
@ConditionalOnProperty(name = "rentaya.seed.enabled", havingValue = "true")
public class DevelopmentDataConfig {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataConfig.class);

    @Bean
    public ApplicationRunner developmentUsers(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        PropertyRepository propertyRepository,
        PhotoRepository photoRepository
    ) {
        return args -> {
            User owner = findOrCreateUser(
                userRepository,
                passwordEncoder,
                "Owner",
                "Front",
                "owner.front@example.com",
                "987654321",
                Rol.PROPIETARIO
            );
            findOrCreateUser(
                userRepository,
                passwordEncoder,
                "Tenant",
                "Front",
                "tenant.front@example.com",
                "912345678",
                Rol.INQUILINO
            );
            createPropertyIfMissing(propertyRepository, photoRepository, owner);
        };
    }

    private User findOrCreateUser(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        String firstName,
        String lastName,
        String email,
        String phone,
        Rol role
    ) {
        var existing = userRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password1"));
        user.setPhone(phone);
        user.setRole(role);
        User saved = userRepository.saveAndFlush(user);
        log.info("Usuario de desarrollo creado: {}", email);
        return saved;
    }

    private void createPropertyIfMissing(
        PropertyRepository propertyRepository,
        PhotoRepository photoRepository,
        User owner
    ) {
        String title = "Departamento demo en Miraflores";
        if (propertyRepository.existsByOwnerIdAndTitle(owner.getId(), title)) {
            return;
        }

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(title);
        property.setDistrict("Miraflores");
        property.setAddress("Av. Demo 123");
        property.setPrice(new BigDecimal("1800.00"));
        property.setBedrooms(2);
        property.setBathrooms(1);
        property.setArea(70);
        property.setDescription("Propiedad disponible para probar la integración del frontend.");
        property.setStatus("Disponible");
        property = propertyRepository.saveAndFlush(property);

        Photo photo = new Photo();
        photo.setProperty(property);
        photo.setUrl("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267");
        photoRepository.save(photo);
        log.info("Propiedad de desarrollo creada: {}", title);
    }
}
