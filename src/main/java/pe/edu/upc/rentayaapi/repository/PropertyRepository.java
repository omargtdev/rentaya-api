package pe.edu.upc.rentayaapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.upc.rentayaapi.model.Property;

public interface PropertyRepository extends JpaRepository<Property, Integer>, JpaSpecificationExecutor<Property> {
    boolean existsByOwnerIdAndTitle(Integer ownerId, String title);
    List<Property> findByOwnerIdOrderByCreatedAtDescIdDesc(Integer ownerId);
}
