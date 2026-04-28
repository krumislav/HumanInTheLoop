package mk.ukim.finki.humanintheloopllm.service.impl;
import mk.ukim.finki.humanintheloopllm.service.OpenRouterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;


@Service
public class OpenRouterServiceImpl implements OpenRouterService {
    private final RestClient restClient;

    @Value("${openrouter.api.key}")
    private String apiKey;

    public OpenRouterServiceImpl(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
    }

    @Override
    public String ask(String prompt, String model) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        List choices = (List) response.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        return message.get("content").toString();
    }
}
