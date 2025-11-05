package rs.kunperooo.dailybot.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Slack user data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackUserRest {

    private String id;
    private String name;
    private String realName;
    private String email;
    private String phone;
    private String title;
    private String timezone;
    private String timezoneLabel;
    private String locale;
    private String teamId;
    private String profileImage24;
    private String profileImage32;
    private String profileImage48;
    private String profileImage72;
    private String profileImage192;
    private String profileImage512;
}