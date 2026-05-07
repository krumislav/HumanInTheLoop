package mk.ukim.finki.humanintheloopllm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "model_ai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelAi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String modelName;

    private String description;
}