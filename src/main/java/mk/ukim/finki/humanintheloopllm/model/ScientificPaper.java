package mk.ukim.finki.humanintheloopllm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class ScientificPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private Integer year;

    private String fileName;

    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaperChunk> chunks = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}