package rs.kunperooo.dailybot.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShowQuestionRest {
    private Integer order;
    private String text;
    private List<ShowAnswerRest> answer;
}
