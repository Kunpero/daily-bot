package rs.kunperooo.dailybot.utils;

import com.slack.api.model.User;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for converting Slack User objects to DTOs
 */
public class SlackUserConverter {

    /**
     * Converts a Slack User to SlackUserDto
     *
     * @param user The Slack User object
     * @return SlackUserDto
     */
    public static SlackUserDto convert(User user) {
        if (user == null) {
            return null;
        }

        return SlackUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .realName(user.getRealName())
                .email(user.getProfile() != null ? user.getProfile().getEmail() : null)
                .phone(user.getProfile() != null ? user.getProfile().getPhone() : null)
                .title(user.getProfile() != null ? user.getProfile().getTitle() : null)
                .timezone(user.getTz())
                .timezoneLabel(user.getTzLabel())
                .locale(user.getLocale())
                .teamId(user.getTeamId())
                .profileImage24(user.getProfile() != null ? user.getProfile().getImage24() : null)
                .profileImage32(user.getProfile() != null ? user.getProfile().getImage32() : null)
                .profileImage48(user.getProfile() != null ? user.getProfile().getImage48() : null)
                .profileImage72(user.getProfile() != null ? user.getProfile().getImage72() : null)
                .profileImage192(user.getProfile() != null ? user.getProfile().getImage192() : null)
                .profileImage512(user.getProfile() != null ? user.getProfile().getImage512() : null)
                .build();
    }

    /**
     * Converts a list of Slack Users to SlackUserDto list
     *
     * @param users List of Slack User objects
     * @return List of SlackUserDto
     */
    public static List<SlackUserDto> convert(List<User> users) {
        if (users == null) {
            return List.of();
        }

        return users.stream()
                .map(SlackUserConverter::convert)
                .toList();
    }

    /**
     * Converts an Optional Slack User to Optional SlackUserDto
     *
     * @param user Optional Slack User object
     * @return Optional SlackUserDto
     */
    public static Optional<SlackUserDto> convert(Optional<User> user) {
        return user.map(SlackUserConverter::convert);
    }
}