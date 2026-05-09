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
                        createModel("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (Free)"),
                        createModel("tencent/hy3-preview:free", "Tencent hy3 (Free)"),
                        createModel("mistralai/mistral-small-3.2-24b-instruct:free", "Mistral Small (Free)"),
                        createModel("microsoft/phi-4-reasoning-plus:free", "Microsoft Phi-4 (Free)")
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