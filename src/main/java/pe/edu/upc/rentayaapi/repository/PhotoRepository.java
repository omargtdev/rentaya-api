package pe.edu.upc.rentayaapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findByPropertyIdOrderById(Integer propertyId);
    void deleteByPropertyId(Integer propertyId);
}
