package pe.edu.upc.rentayaapi.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.rentayaapi.dto.VisitRequest;
import pe.edu.upc.rentayaapi.dto.VisitResponse;
import pe.edu.upc.rentayaapi.dto.VisitStatusRequest;
import pe.edu.upc.rentayaapi.exception.ApiException;
import pe.edu.upc.rentayaapi.model.Property;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.model.Visit;
import pe.edu.upc.rentayaapi.repository.VisitRepository;

@Service
public class VisitService {

    private static final String PENDING = "Pendiente";
    private static final Set<LocalTime> ALLOWED_TIMES = Set.of(
        LocalTime.of(8, 0),
        LocalTime.of(9, 0),
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        LocalTime.of(14, 0),
        LocalTime.of(15, 0),
        LocalTime.of(17, 0),
        LocalTime.of(19, 0)
    );

    private final VisitRepository visitRepository;
    private final PropertyService propertyService;
    private final CurrentUserService currentUserService;

    public VisitService(
        VisitRepository visitRepository,
        PropertyService propertyService,
        CurrentUserService currentUserService
    ) {
        this.visitRepository = visitRepository;
        this.propertyService = propertyService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public VisitResponse create(VisitRequest request) {
        User tenant = currentUserService.get();
        requireRole(tenant, Rol.INQUILINO, "Solo los inquilinos pueden solicitar visitas");
        Property property = propertyService.find(request.propertyId());

        if (!"Disponible".equals(property.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La propiedad no está disponible");
        }
        if (property.getOwner().getId().equals(tenant.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes solicitar una visita a tu propia propiedad");
        }
        if (!ALLOWED_TIMES.contains(request.time())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selecciona un horario de visita válido");
        }
        if (visitRepository.existsByPropertyIdAndTenantIdAndStatus(property.getId(), tenant.getId(), PENDING)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya tienes una solicitud pendiente para esta propiedad.");
        }

        Visit visit = new Visit();
        visit.setProperty(property);
        visit.setTenant(tenant);
        visit.setDate(request.date());
        visit.setTime(request.time());
        visit.setStatus(PENDING);
        return toResponse(visitRepository.saveAndFlush(visit));
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> ownerVisits(String status) {
        User owner = currentUserService.get();
        requireRole(owner, Rol.PROPIETARIO, "Solo los propietarios pueden revisar solicitudes recibidas");
        validateFilter(status);
        return visitRepository.findByPropertyOwnerIdOrderByCreatedAtDesc(owner.getId()).stream()
            .filter(visit -> status == null || status.isBlank() || visit.getStatus().equals(status))
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> tenantVisits() {
        User tenant = currentUserService.get();
        requireRole(tenant, Rol.INQUILINO, "Solo los inquilinos pueden revisar sus solicitudes");
        return visitRepository.findByTenantIdOrderByCreatedAtDesc(tenant.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public VisitResponse updateStatus(Integer id, VisitStatusRequest request) {
        User owner = currentUserService.get();
        requireRole(owner, Rol.PROPIETARIO, "Solo los propietarios pueden gestionar solicitudes");
        Visit visit = visitRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud de visita no encontrada"));

        if (!visit.getProperty().getOwner().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes gestionar visitas de otra propiedad");
        }
        if (!PENDING.equals(visit.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La solicitud ya fue resuelta");
        }

        visit.setStatus(request.status());
        return toResponse(visitRepository.saveAndFlush(visit));
    }

    private VisitResponse toResponse(Visit visit) {
        return new VisitResponse(
            visit.getId(),
            visit.getProperty().getId(),
            visit.getProperty().getTitle(),
            visit.getTenant().getId(),
            PropertyService.fullName(visit.getTenant()),
            visit.getProperty().getOwner().getId(),
            visit.getDate(),
            visit.getTime(),
            visit.getStatus(),
            visit.getCreatedAt()
        );
    }

    private void requireRole(User user, Rol role, String message) {
        if (user.getRole() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void validateFilter(String status) {
        if (status != null && !status.isBlank() && !Set.of("Pendiente", "Aceptada", "Rechazada").contains(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado de visita inválido");
        }
    }
}
