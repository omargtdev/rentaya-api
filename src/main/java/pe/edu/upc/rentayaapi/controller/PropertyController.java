package pe.edu.upc.rentayaapi.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.rentayaapi.dto.PropertyRequest;
import pe.edu.upc.rentayaapi.dto.PropertyResponse;
import pe.edu.upc.rentayaapi.service.PropertyService;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<PropertyResponse> list(
        @RequestParam(required = false) String district,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return propertyService.list(district, minPrice, maxPrice);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROPIETARIO')")
    public List<PropertyResponse> mine() {
        return propertyService.mine();
    }

    @GetMapping("/{id}")
    public PropertyResponse get(@PathVariable Integer id) {
        return propertyService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROPIETARIO')")
    public PropertyResponse update(@PathVariable Integer id, @Valid @RequestBody PropertyRequest request) {
        return propertyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        propertyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
