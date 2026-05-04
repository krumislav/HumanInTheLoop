package mk.ukim.finki.humanintheloopllm.controller;


import mk.ukim.finki.humanintheloopllm.model.ModelAi;
import mk.ukim.finki.humanintheloopllm.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final List<ModelAi> modelList;

    public ChatController(ChatService chatService, List<ModelAi> modelList) {
        this.chatService = chatService;
        this.modelList = modelList;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String chatPage(Model model) {
        model.addAttribute("messages", chatService.getAllMessages());
        model.addAttribute("modelsss", modelList);
        return "chat";
    }

    @PostMapping("/chat/send")
    public String sendMessage(@RequestParam String prompt,
                              @RequestParam String model) {
        try {
            chatService.sendMessage(prompt, model);
        } catch (RuntimeException e) {
            return "redirect:/chat?error=ratelimit";
        }
        return "redirect:/chat";
    }

    @PostMapping("/chat/{id}/approve")
    public String approve(@PathVariable Long id) {
        chatService.approve(id);
        return "redirect:/chat";
    }

    @PostMapping("/chat/{id}/reject")
    public String reject(@PathVariable Long id) {
        chatService.reject(id);
        return "redirect:/chat";
    }

    @PostMapping("/chat/{id}/correct")
    public String correct(@PathVariable Long id,
                          @RequestParam String correctedResponse) {
        chatService.correct(id, correctedResponse);
        return "redirect:/chat";
    }
}

