package mk.ukim.finki.humanintheloopllm.service;

import mk.ukim.finki.humanintheloopllm.model.ModelAi;

import java.util.List;

public interface ModelAiService {
    List<ModelAi> listModels();
}