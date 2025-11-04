package rs.kunperooo.dailybot.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CheckInSubmissionMeta {
    private String responseUrl;
    private UUID historyUuid;
}
