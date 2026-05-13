package mk.ukim.finki.humanintheloopllm.service.impl;

import mk.ukim.finki.humanintheloopllm.model.PaperChunk;
import mk.ukim.finki.humanintheloopllm.service.OpenRouterService;
import mk.ukim.finki.humanintheloopllm.service.ScientificPaperService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterServiceImpl implements OpenRouterService {

    private final RestClient restClient;
    private final ScientificPaperService paperService;

    @Value("${openrouter.api.key}")
    private String apiKey;

    public OpenRouterServiceImpl(RestClient.Builder builder, ScientificPaperService paperService) {
        this.restClient = builder
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
        this.paperService = paperService;
    }

    @Override
    public String ask(String prompt, String model) {

        List<PaperChunk> relevantChunks = paperService.searchChunks(prompt);

        String systemPrompt;

        if (relevantChunks.isEmpty()) {
            systemPrompt = "You are a helpful assistant.";
        } else {
            StringBuilder context = new StringBuilder();
            context.append("You are a helpful assistant. Answer the user's question based ONLY on the following excerpts from scientific papers. ");
            context.append("Always mention which paper the information comes from.\n\n");
            context.append("=== RELEVANT EXCERPTS FROM SCIENTIFIC PAPERS ===\n\n");

            for (PaperChunk chunk : relevantChunks) {
                context.append("--- From: \"")
                       .append(chunk.getPaper().getTitle())
                       .append("\" by ")
                       .append(chunk.getPaper().getAuthor())
                       .append(" (")
                       .append(chunk.getPaper().getYear())
                       .append(") ---\n");
                context.append(chunk.getContent());
                context.append("\n\n");
            }

            context.append("=== END OF EXCERPTS ===\n\n");
            context.append("If the answer cannot be found in the provided excerpts, say: 'I could not find information about this in the provided scientific papers.'");

            systemPrompt = context.toString();
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", prompt)
                )
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 429) {
                        throw new RuntimeException("Rate limit reached. Please wait a moment and try again.");
                    }
                    throw new RuntimeException("API error: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("OpenRouter server error. Please try again later.");
                })
                .body(Map.class);

        List choices = (List) response.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        return message.get("content").toString();
    }
}