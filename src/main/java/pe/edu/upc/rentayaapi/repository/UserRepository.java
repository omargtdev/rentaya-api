package pe.edu.upc.rentayaapi.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.User;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
    Optional<User> findByEmailIgnoreCase(String email);
}
