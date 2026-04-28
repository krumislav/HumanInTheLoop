package mk.ukim.finki.humanintheloopllm.controller;


import mk.ukim.finki.humanintheloopllm.model.ModelAi;
import mk.ukim.finki.humanintheloopllm.repository.ChatMessageRepository;
import mk.ukim.finki.humanintheloopllm.service.ChatService;
import mk.ukim.finki.humanintheloopllm.service.OpenRouterService;
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
    public String chatPage(Model model) {
        model.addAttribute("messages", chatService.getAllMessages());

        model.addAttribute("modelsss",modelList);
        model.addAttribute("models", List.of(
                "google/gemma-4-26b-a4b-it:free",
                "nvidia/nemotron-3-super-120b-a12b:free",
                "qwen/qwen3-next-80b-a3b-instruct:free",
                "meta-llama/llama-3.3-70b-instruct:free"
        ));

        return "chat";
    }

    @PostMapping("/chat/send")
    public String sendMessage(@RequestParam String prompt,
                              @RequestParam String model) {
        chatService.sendMessage(prompt, model);
        return "redirect:/";
    }

    @PostMapping("/chat/{id}/approve")
    public String approve(@PathVariable Long id) {
        chatService.approve(id);
        return "redirect:/";
    }

    @PostMapping("/chat/{id}/reject")
    public String reject(@PathVariable Long id) {
        chatService.reject(id);
        return "redirect:/";
    }

    @PostMapping("/chat/{id}/correct")
    public String correct(@PathVariable Long id,
                          @RequestParam String correctedResponse) {
        chatService.correct(id, correctedResponse);
        return "redirect:/";
    }
}

