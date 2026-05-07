package mk.ukim.finki.humanintheloopllm.web.rest;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import mk.ukim.finki.humanintheloopllm.service.ChatService;
import mk.ukim.finki.humanintheloopllm.web.dto.DashboardStats;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {

    private final ChatService chatService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardStats getStats() {
        return chatService.getDashboardStats();
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ChatMessage> getAllMessages() {
        return chatService.getAllMessages();
    }
}