package rs.kunperooo.dailybot.controller.dto.history;

import lombok.Builder;
import lombok.Data;
import rs.kunperooo.dailybot.controller.dto.form.SlackUserRest;

@Data
@Builder
public class ShowAnswerRest {
    private SlackUserRest slackUser;
    private String text;
}
