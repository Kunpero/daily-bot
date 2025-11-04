package rs.kunperooo.dailybot.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SaveAnswersDto {
    private UUID checkInUuid;

    private String userId;

    private String responseUrl;

    List<AnswerDto> answers;
}
