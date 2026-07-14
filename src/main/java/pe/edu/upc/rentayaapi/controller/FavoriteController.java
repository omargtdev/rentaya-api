package pe.edu.upc.rentayaapi.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.rentayaapi.dto.PropertyResponse;
import pe.edu.upc.rentayaapi.service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<PropertyResponse> list() {
        return favoriteService.list();
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<Void> add(@PathVariable Integer propertyId) {
        favoriteService.add(propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> remove(@PathVariable Integer propertyId) {
        favoriteService.remove(propertyId);
        return ResponseEntity.noContent().build();
    }
}
