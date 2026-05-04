package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.model.PaperChunk;
import mk.ukim.finki.humanintheloopllm.model.ScientificPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperChunkRepository extends JpaRepository<PaperChunk, Long> {

    List<PaperChunk> findByContentContainingIgnoreCase(String keyword);

    List<PaperChunk> findByPaper(ScientificPaper paper);
}