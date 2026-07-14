package pe.edu.upc.rentayaapi.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogs")
public class CatalogController {

    @GetMapping("/districts")
    public List<String> districts() {
        return List.of("Miraflores", "Surco", "San Borja", "Lince", "Jesús María", "Barranco");
    }
}
