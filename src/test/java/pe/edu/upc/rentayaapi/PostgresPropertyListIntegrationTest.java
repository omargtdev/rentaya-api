package pe.edu.upc.rentayaapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upc.rentayaapi.model.Photo;
import pe.edu.upc.rentayaapi.model.Property;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.PhotoRepository;
import pe.edu.upc.rentayaapi.repository.PropertyRepository;
import pe.edu.upc.rentayaapi.repository.UserRepository;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "rentaya.seed.enabled=false"
})
class PostgresPropertyListIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Test
    void publicListSupportsNullAndPresentOptionalFiltersOnPostgres() throws Exception {
        User owner = new User();
        owner.setFirstName("Owner");
        owner.setLastName("Postgres");
        owner.setEmail("owner-postgres@rentaya.test");
        owner.setPassword("not-used-in-this-test");
        owner.setPhone("945678901");
        owner.setRole(Rol.PROPIETARIO);
        owner = userRepository.saveAndFlush(owner);

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle("Departamento PostgreSQL");
        property.setDistrict("Miraflores");
        property.setAddress("Av. PostgreSQL 17");
        property.setPrice(new BigDecimal("1800.00"));
        property.setBedrooms(2);
        property.setBathrooms(1);
        property.setArea(70);
        property.setDescription("Regresión de filtros opcionales tipados.");
        property.setStatus("Disponible");
        property = propertyRepository.saveAndFlush(property);

        Photo photo = new Photo();
        photo.setProperty(property);
        photo.setUrl("https://example.com/postgres.jpg");
        photoRepository.saveAndFlush(photo);

        mockMvc.perform(get("/api/properties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(property.getId()));
        mockMvc.perform(get("/api/properties").param("district", "miraflores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(property.getId()));
        mockMvc.perform(get("/api/properties").param("district", " "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(property.getId()));
        mockMvc.perform(get("/api/properties").param("minPrice", "1700"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(property.getId()));
        mockMvc.perform(get("/api/properties").param("maxPrice", "1900"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(property.getId()));
        mockMvc.perform(get("/api/properties")
                .param("district", "Miraflores")
                .param("minPrice", "1700")
                .param("maxPrice", "1900"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].photos[0]").value("https://example.com/postgres.jpg"));
        mockMvc.perform(get("/api/properties").param("minPrice", "1901"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }
}
