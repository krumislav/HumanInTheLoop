package mk.ukim.finki.humanintheloopllm.repository;

import mk.ukim.finki.humanintheloopllm.model.PaperChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperChunkRepository extends JpaRepository<PaperChunk, Long> {

    @Query("SELECT pc FROM PaperChunk pc WHERE LOWER(pc.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PaperChunk> findByContentContainingIgnoreCase(@Param("keyword") String keyword);
}