package mk.ukim.finki.humanintheloopllm.service;

import mk.ukim.finki.humanintheloopllm.model.User;
import mk.ukim.finki.humanintheloopllm.enums.Role;

public interface AuthService {
    User register(String username, String password, Role role);
    String login(String username, String password);
}