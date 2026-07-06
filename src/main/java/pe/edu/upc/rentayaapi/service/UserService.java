package pe.edu.upc.rentayaapi.service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.rentayaapi.dto.RegisterRequest;
import pe.edu.upc.rentayaapi.dto.RegisterResponse;
import pe.edu.upc.rentayaapi.exception.EmailAlreadyExistsException;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(request.role());
        User saved = userRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getEmail(), saved.getPhone(), saved.getRole());
    }
}
