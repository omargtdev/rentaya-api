package pe.edu.upc.rentayaapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.Visit;

public interface VisitRepository extends JpaRepository<Visit, Integer> {
    boolean existsByPropertyIdAndTenantIdAndStatus(Integer propertyId, Integer tenantId, String status);
    List<Visit> findByPropertyOwnerIdOrderByCreatedAtDesc(Integer ownerId);
    List<Visit> findByTenantIdOrderByCreatedAtDesc(Integer tenantId);
}
