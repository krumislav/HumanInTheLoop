package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.model.ScientificPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScientificPaperRepository extends JpaRepository<ScientificPaper, Long> {
}