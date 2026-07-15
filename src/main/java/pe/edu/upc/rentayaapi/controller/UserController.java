package pe.edu.upc.rentayaapi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.rentayaapi.dto.RegisterRequest;
import pe.edu.upc.rentayaapi.dto.RegisterResponse;
import pe.edu.upc.rentayaapi.dto.UpdateUserRequest;
import pe.edu.upc.rentayaapi.dto.UserResponse;
import pe.edu.upc.rentayaapi.service.UserService;
import pe.edu.upc.rentayaapi.service.UserProfileService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    public UserController(UserService userService, UserProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userProfileService.me();
    }

    @PatchMapping("/me")
    public UserResponse update(@Valid @RequestBody UpdateUserRequest request) {
        return userProfileService.update(request);
    }
}
