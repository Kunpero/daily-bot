package rs.kunperooo.dailybot.service.dto.history;

import lombok.Builder;
import lombok.Data;
import rs.kunperooo.dailybot.controller.dto.ShowAnswerRest;

import java.util.List;

@Data
@Builder
public class ShowQuestionDto {
    private Integer order;
    private String text;
    private List<ShowAnswerDto> answers;
}
