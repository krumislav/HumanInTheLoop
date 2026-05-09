package mk.ukim.finki.humanintheloopllm.service;

import mk.ukim.finki.humanintheloopllm.model.PaperChunk;
import mk.ukim.finki.humanintheloopllm.model.ScientificPaper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ScientificPaperService {

    ScientificPaper uploadPaper(MultipartFile file, String title, String author, Integer year);

    List<ScientificPaper> findAll();

    List<PaperChunk> searchChunks(String query);

    void deletePaper(Long id);

    void updatePaper(Long id, String title, String author, Integer year);
}