package rs.kunperooo.dailybot.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SaveAnswersDto {

    private String userId;

    private String callbackId;

    List<AnswerDto> answers;
}
