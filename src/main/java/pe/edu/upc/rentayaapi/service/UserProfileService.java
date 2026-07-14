package pe.edu.upc.rentayaapi.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.rentayaapi.dto.UpdateUserRequest;
import pe.edu.upc.rentayaapi.dto.UserResponse;
import pe.edu.upc.rentayaapi.exception.EmailAlreadyExistsException;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.UserRepository;

@Service
public class UserProfileService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public UserProfileService(CurrentUserService currentUserService, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(currentUserService.get());
    }

    @Transactional
    public UserResponse update(UpdateUserRequest request) {
        User user = currentUserService.get();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
            throw new EmailAlreadyExistsException(email);
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException(email);
        }
    }
}
