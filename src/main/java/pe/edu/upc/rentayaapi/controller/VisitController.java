package pe.edu.upc.rentayaapi.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.rentayaapi.dto.VisitRequest;
import pe.edu.upc.rentayaapi.dto.VisitResponse;
import pe.edu.upc.rentayaapi.dto.VisitStatusRequest;
import pe.edu.upc.rentayaapi.service.VisitService;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    @PreAuthorize("hasRole('INQUILINO')")
    public ResponseEntity<VisitResponse> create(@Valid @RequestBody VisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.create(request));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('PROPIETARIO')")
    public List<VisitResponse> ownerVisits(@RequestParam(required = false) String status) {
        return visitService.ownerVisits(status);
    }

    @GetMapping("/tenant")
    @PreAuthorize("hasRole('INQUILINO')")
    public List<VisitResponse> tenantVisits() {
        return visitService.tenantVisits();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PROPIETARIO')")
    public VisitResponse updateStatus(@PathVariable Integer id, @Valid @RequestBody VisitStatusRequest request) {
        return visitService.updateStatus(id, request);
    }
}
