package pe.edu.upc.rentayaapi.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.User;
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
}
