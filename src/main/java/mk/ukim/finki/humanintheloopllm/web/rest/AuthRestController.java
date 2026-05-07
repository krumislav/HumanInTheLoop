package mk.ukim.finki.humanintheloopllm.web.rest;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.model.User;
import mk.ukim.finki.humanintheloopllm.repository.UserRepository;
import mk.ukim.finki.humanintheloopllm.service.AuthService;
import mk.ukim.finki.humanintheloopllm.web.dto.AuthResponse;
import mk.ukim.finki.humanintheloopllm.web.dto.LoginRequest;
import mk.ukim.finki.humanintheloopllm.web.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), "ROLE_" + user.getRole().name()));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request.getUsername(), request.getPassword(), request.getRole());
            String token = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), "ROLE_" + user.getRole().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}