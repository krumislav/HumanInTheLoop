package mk.ukim.finki.humanintheloopllm.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.model.ModelAi;
import mk.ukim.finki.humanintheloopllm.repository.ModelRepository;
import mk.ukim.finki.humanintheloopllm.service.ModelAiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelAiServiceImpl implements ModelAiService {

    private final ModelRepository modelRepository;

    @Override
    public List<ModelAi> listModels() {
        return modelRepository.findAll();
    }
}