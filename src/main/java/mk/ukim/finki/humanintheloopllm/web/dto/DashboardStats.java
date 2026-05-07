package mk.ukim.finki.humanintheloopllm.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalMessages;
    private long approvedCount;
    private long rejectedCount;
    private long correctedCount;
    private long pendingCount;
}