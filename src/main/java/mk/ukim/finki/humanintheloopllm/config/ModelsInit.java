package mk.ukim.finki.humanintheloopllm.config;

import jakarta.annotation.PostConstruct;

import mk.ukim.finki.humanintheloopllm.model.ModelAi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;


// model.addAttribute("modelsname",List.of(
//                            "Google Gemma 4",
//                "Nvidia Nemotron 3",
//                            "Qwen 3",
//                            "Llama 3.3"));
//        model.addAttribute("models", List.of(
//                                   "google/gemma-4-26b-a4b-it:free",
//                "nvidia/nemotron-3-super-120b-a12b:free",
//                                   "qwen/qwen3-next-80b-a3b-instruct:free",
//                                   "meta-llama/llama-3.3-70b-instruct:free"
//));

@Configuration
public class ModelsInit {

    @Bean
    public List<ModelAi> models(){
        return List.of(
                new ModelAi("google/gemma-4-26b-a4b-it:free","Google Gemma 4"),
                new ModelAi("nvidia/nemotron-3-super-120b-a12b:free","Nvidia Nemotron 3"),
                new ModelAi("qwen/qwen3-next-80b-a3b-instruct:free","Qwen 3"),
                new ModelAi("meta-llama/llama-3.3-70b-instruct:free","Llama 3.3"),
                new ModelAi("tencent/hy3-preview:free","Tencent hy3")
        );
    }
}
