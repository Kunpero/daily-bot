package rs.kunperooo.dailybot.service.dto.history;

import lombok.Builder;
import lombok.Data;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;

@Data
@Builder
public class ShowAnswerDto {
    private SlackUserDto slackUser;
    private String text;
}
