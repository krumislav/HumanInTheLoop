package mk.ukim.finki.humanintheloopllm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.humanintheloopllm.enums.ReviewStatus;
import org.hibernate.type.descriptor.jdbc.NVarcharJdbcType;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(columnDefinition = "NVARCHAR(MAX)")
    @Column(columnDefinition = "TEXT")
    private String userPrompt;

    // @Column(columnDefinition = "NVARCHAR(MAX)")
    @Column(columnDefinition = "TEXT")
    private String assistantResponse;

    // @Column(columnDefinition = "NVARCHAR(MAX)")
    @Column(columnDefinition = "TEXT")
    private String correctedResponse;

    private String modelName;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = ReviewStatus.PENDING;
    }
    @ManyToOne
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;
}