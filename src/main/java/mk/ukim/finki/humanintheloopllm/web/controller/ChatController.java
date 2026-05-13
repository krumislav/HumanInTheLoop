package mk.ukim.finki.humanintheloopllm.web.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.model.ChatMessage;
import mk.ukim.finki.humanintheloopllm.model.ChatSession;
import mk.ukim.finki.humanintheloopllm.model.User;
import mk.ukim.finki.humanintheloopllm.repository.UserRepository;
import mk.ukim.finki.humanintheloopllm.service.ChatService;
import mk.ukim.finki.humanintheloopllm.service.ModelAiService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ModelAiService modelAiService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                return userRepository.findByUsername(username).orElse(null);
            }

            if (principal instanceof User) {
                return (User) principal;
            }
        }
        return null;
    }
    @GetMapping
    public String chatPage(@RequestParam(required = false) Long sessionId, Model model) {
        User currentUser = getCurrentUser();

        System.out.println("==================== DEBUG ====================");
        System.out.println("Current User: " + currentUser);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + auth);
        System.out.println("Principal: " + (auth != null ? auth.getPrincipal() : "null"));
        System.out.println("Principal Class: " + (auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass() : "null"));
        System.out.println("===============================================");

        List<ChatSession> sessions;

        if (currentUser != null) {
            sessions = chatService.getSessionsByUser(currentUser);

            if (sessionId == null) {
                if (sessions.isEmpty()) {
                    ChatSession newSession = chatService.createNewSession(currentUser);
                    sessionId = newSession.getId();
                    sessions = chatService.getSessionsByUser(currentUser);
                } else {
                    sessionId = sessions.get(0).getId();
                }
            }
        } else {
            sessions = chatService.getAllSessions();

            if (sessionId == null) {
                if (sessions.isEmpty()) {
                    ChatSession newSession = chatService.createNewSession(null);
                    sessionId = newSession.getId();
                    sessions = chatService.getAllSessions();
                } else {
                    sessionId = sessions.get(0).getId();
                }
            }
        }

        ChatSession currentSession = chatService.getSessionById(sessionId);

        if (currentUser != null && currentSession.getUser() != null &&
                !currentSession.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/chat";
        }

        List<ChatMessage> messages = chatService.getMessagesBySession(sessionId);

        model.addAttribute("sessions", sessions);
        model.addAttribute("currentSession", currentSession);
        model.addAttribute("messages", messages);
        model.addAttribute("modelsss", modelAiService.listModels());

        return "chat";
    }

    @PostMapping("/new")
    public String createNewChat() {
        User currentUser = getCurrentUser();
        ChatSession newSession = chatService.createNewSession(currentUser);
        return "redirect:/chat?sessionId=" + newSession.getId();
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam Long sessionId,
                              @RequestParam String prompt,
                              @RequestParam String model) {
        User currentUser = getCurrentUser();

        ChatSession session = chatService.getSessionById(sessionId);
        if (currentUser != null && session.getUser() != null &&
                !session.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/chat";
        }

        try {
            chatService.sendPromptAndSaveResponse(sessionId, prompt, model);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/chat?sessionId=" + sessionId + "&error=failed";
        }
        return "redirect:/chat?sessionId=" + sessionId;
    }

    @PostMapping("/{id}/approve")
    public String approveMessage(@PathVariable Long id, @RequestParam Long sessionId) {
        chatService.approveMessage(id);
        return "redirect:/chat?sessionId=" + sessionId;
    }

    @PostMapping("/{id}/reject")
    public String rejectMessage(@PathVariable Long id, @RequestParam Long sessionId) {
        chatService.rejectMessage(id);
        return "redirect:/chat?sessionId=" + sessionId;
    }

    @PostMapping("/{id}/correct")
    public String correctMessage(@PathVariable Long id,
                                 @RequestParam Long sessionId,
                                 @RequestParam String correctedResponse) {
        chatService.correctMessage(id, correctedResponse);
        return "redirect:/chat?sessionId=" + sessionId;
    }

    @PostMapping("/{sessionId}/rename")
    public String renameSession(@PathVariable Long sessionId,
                                @RequestParam String newTitle) {
        chatService.renameSession(sessionId, newTitle);
        return "redirect:/chat?sessionId=" + sessionId;
    }
}