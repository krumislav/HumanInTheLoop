 package mk.ukim.finki.humanintheloopllm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class PaperChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer pageNumber;

    private Integer chunkIndex;

    @ManyToOne
    @JoinColumn(name = "paper_id")
    private ScientificPaper paper;
}