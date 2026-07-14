package pe.edu.upc.rentayaapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserIdOrderByIdDesc(Integer userId);
    boolean existsByUserIdAndPropertyId(Integer userId, Integer propertyId);
    Optional<Favorite> findByUserIdAndPropertyId(Integer userId, Integer propertyId);
}
