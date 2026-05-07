package mk.ukim.finki.humanintheloopllm.config;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.model.ModelAi;
import mk.ukim.finki.humanintheloopllm.repository.ModelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ModelsInit {

    @Bean
    public CommandLineRunner initModels(ModelRepository modelRepository) {
        return args -> {
            if (modelRepository.count() == 0) {
                List<ModelAi> models = List.of(
                        createModel("openai/gpt-4o", "GPT-4 Omni"),
                        createModel("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet"),
                        createModel("google/gemini-pro", "Google Gemini Pro"),
                        createModel("meta-llama/llama-3.1-70b-instruct", "Llama 3.1 70B"),
                        createModel("mistralai/mistral-large", "Mistral Large")
                );
                modelRepository.saveAll(models);
            }
        };
    }

    private ModelAi createModel(String modelName, String description) {
        ModelAi model = new ModelAi();
        model.setModelName(modelName);
        model.setDescription(description);
        return model;
    }
}