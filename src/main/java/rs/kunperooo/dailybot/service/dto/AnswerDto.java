package rs.kunperooo.dailybot.service.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AnswerDto {
    private UUID uuid;

    private UUID questionInHistoryUuid;

    private String answer;
}
