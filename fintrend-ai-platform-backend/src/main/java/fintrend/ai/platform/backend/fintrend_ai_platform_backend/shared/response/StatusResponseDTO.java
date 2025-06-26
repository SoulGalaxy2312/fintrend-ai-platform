package fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusResponseDTO {
    private boolean success;
    private String message;
}
