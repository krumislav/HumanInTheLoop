package mk.ukim.finki.humanintheloopllm.web.dto;

import lombok.Data;
import mk.ukim.finki.humanintheloopllm.enums.Role;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}