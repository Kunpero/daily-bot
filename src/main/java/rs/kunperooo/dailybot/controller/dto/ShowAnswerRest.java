package rs.kunperooo.dailybot.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShowAnswerRest {
    private SlackUserRest slackUser;
    private String text;
}
