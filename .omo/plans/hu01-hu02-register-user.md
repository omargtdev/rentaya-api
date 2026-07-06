# Plan: HU01/HU02 — Register User (API + WEB)

## Branch
`feat/og/register.user` (already merged with latest main in both repos)

## Repos
- API: `/home/linuxero/repos/upc/rentaya-api`
- WEB: `/home/linuxero/repos/upc/rentaya-web`

## Confirmed decisions
1. No navbar on register/login pages (those nav links are for logged users)
2. Email duplicate check: Option B — rely on POST /api/users/register returning 409
3. Phone validation: validate on backend too (Perú 9 digits regex)
4. Existing feat/og/register.user branch used (merged with main)
5. Add tests to the web app too
6. Do NOT create PRs — just push the branches

---

## API Implementation

### Current state
- Spring Security dependency already in pom.xml
- Entities are English (User, Property, Photo, Visit, Message, Favorite)
- User.role is String, needs to become Rol enum
- Branch: feat/og/register.user, up to date with main

### Files to CREATE

#### model/Rol.java
```java
package pe.edu.upc.rentayaapi.model;

public enum Rol {
    PROPIETARIO,
    INQUILINO
}
```

#### model/User.java (UPDATE)
Change `role` field from String to Rol:
```java
@Enumerated(EnumType.STRING)
@Column(name = "rol", nullable = false, length = 20)
private Rol role;
```

#### dto/RegisterRequest.java
```java
package pe.edu.upc.rentayaapi.dto;

import jakarta.validation.constraints.*;
import pe.edu.upc.rentayaapi.model.Rol;

public record RegisterRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    String firstName,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    String lastName,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido")
    @Size(max = 100)
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "Mínimo 8 caracteres, 1 mayúscula y 1 número")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$", message = "Mínimo 8 caracteres, 1 mayúscula y 1 número")
    String password,

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{9}$", message = "Ingresa un teléfono válido")
    String phone,

    @NotNull(message = "El rol es obligatorio")
    Rol role
) {}
```

#### dto/RegisterResponse.java
```java
package pe.edu.upc.rentayaapi.dto;

import pe.edu.upc.rentayaapi.model.Rol;

public record RegisterResponse(
    Integer id,
    String firstName,
    String lastName,
    String email,
    String phone,
    Rol role
) {}
```

#### repository/UserRepository.java
```java
package pe.edu.upc.rentayaapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
}
```

#### config/SecurityConfig.java
```java
package pe.edu.upc.rentayaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
```

#### exception/EmailAlreadyExistsException.java
```java
package pe.edu.upc.rentayaapi.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("El correo ya está registrado: " + email);
    }
}
```

#### exception/GlobalExceptionHandler.java
```java
package pe.edu.upc.rentayaapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField, e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value"));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("email", "El correo ya está registrado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Internal server error"));
    }
}
```

#### service/UserService.java
```java
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

        return new RegisterResponse(
            saved.getId(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getEmail(),
            saved.getPhone(),
            saved.getRole()
        );
    }
}
```

#### controller/UserController.java
```java
package pe.edu.upc.rentayaapi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.rentayaapi.dto.RegisterRequest;
import pe.edu.upc.rentayaapi.dto.RegisterResponse;
import pe.edu.upc.rentayaapi.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### Tests to CREATE

#### UserServiceTest.java (unit test with Mockito)
- test register success
- test register duplicate email throws EmailAlreadyExistsException
- test password is hashed (not stored in plaintext)

#### UserControllerTest.java (integration test with MockMvc)
- test register success returns 201
- test register with invalid email returns 400
- test register with duplicate email returns 409
- test register with weak password returns 400

---

## WEB Implementation

### Current state
- Angular 22, Tailwind CSS v4 with custom theme (primary, success, danger, warning colors)
- Routes empty, no HttpClient configured
- Branch: feat/og/register.user, up to date with main

### Files to CREATE/UPDATE

#### app.config.ts (UPDATE)
Add provideHttpClient():
```typescript
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

#### app.routes.ts (UPDATE)
```typescript
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'register', loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: '', redirectTo: '/register', pathMatch: 'full' }
];
```

#### services/auth.service.ts
- register(credentials): Observable<RegisterResponse>
- POST to /api/users/register via proxy
- Returns observable for success, throws error for 409 (duplicate email)

#### pages/register/register.component.ts
- Reactive form with FormBuilder
- Fields: firstName, lastName, email, password, phone, role (radio: INQUILINO/PROPIETARIO)
- Validators:
  - firstName: required
  - lastName: required
  - email: required, email format
  - password: required, min 8, pattern (1 uppercase, 1 number)
  - phone: required, pattern (9 digits)
  - role: required, default INQUILINO
- onSubmit(): call AuthService.register(), handle success/error
- Success: show success banner, redirect to /login after 2s
- Error 409: show "El correo ya está registrado" inline or banner
- Loading state: disable button, show spinner
- Form invalid: disable submit button

#### pages/register/register.html
- Two-column layout: grid grid-cols-1 md:grid-cols-[40%_60%]
- Left card: bg-primary-50 border-primary-100 — info panel with title, paragraph, bullet list
- Right card: bg-white border-gray-200 — form with role selector and inputs
- Role selector: grid grid-cols-2 radio group (Inquilino active = primary-600, Propietario inactive)
- Form fields grid 2 columns
- Password help text: text-xs text-gray-400
- Submit button: w-full bg-primary-600 hover:bg-primary-700
- Error messages: text-sm text-danger-600
- Success banner: bg-success-50 border-success-600 text-success-700
- Error banner: bg-danger-50 border-danger-600 text-danger-700

#### pages/login/login.component.ts + login.html
- Simple placeholder page: "Próximamente" message centered
- Title: "Login" or "Iniciar sesión"
- Link back to /register

### Tests to CREATE

#### services/auth.service.spec.ts
- test register success
- test register with duplicate email (HTTP 409 error)

#### pages/register/register.component.spec.ts
- test form is invalid when empty
- test form is valid with correct data
- test email validation shows error for invalid format
- test password validation shows error for weak password
- test phone validation shows error for invalid format
- test role selection defaults to INQUILINO
- test onSubmit calls AuthService register

---

## Commit Strategy
- API: multiple atomic commits per the git-master skill
- WEB: multiple atomic commits per the git-master skill
- Push branches to origin (no PRs)

## Verification
- API: ./mvnw clean test (all tests pass)
- WEB: ng test (all tests pass), ng build (no errors)