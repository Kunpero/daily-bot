package rs.kunperooo.dailybot.service;

import com.slack.api.Slack;
import com.slack.api.app_backend.interactive_components.ActionResponseSender;
import com.slack.api.app_backend.interactive_components.response.ActionResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.model.view.View;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;
import rs.kunperooo.dailybot.utils.SlackUserConverter;

import java.io.IOException;
import java.util.List;

import static rs.kunperooo.dailybot.utils.ActionId.FINISH_CHECK_IN;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackApiService {

    public static final String PLAIN_TEXT_TYPE = "plain_text";

    @Value("${slack.bot.token:}")
    private String slackBotToken;

    private final Slack slack;
    private final ActionResponseSender actionResponseSender;

    /**
     * Retrieves active users only (excluding deleted and bot users)
     *
     * @return List of active Slack users
     * @throws SlackApiException if Slack API returns an error
     * @throws IOException       if there's a network error
     */
    @SneakyThrows
    public List<SlackUserDto> getActiveUsers() {
        log.info("Retrieving active users from Slack workspace");

        List<User> allUsers = getAllUsers();

        List<User> activeUsers = allUsers.stream()
                .filter(user -> !user.isDeleted())
                .filter(user -> !user.isBot())
                .filter(user -> !user.isRestricted())
                .filter(user -> !user.isUltraRestricted())
                .toList();

        List<SlackUserDto> userDtos = SlackUserConverter.convert(activeUsers);
        log.info("Retrieved {} active users from {} total users", activeUsers.size(), allUsers.size());

        return userDtos;
    }

    /**
     * Retrieves user by id
     *
     * @return Slack user
     * @throws SlackApiException if Slack API returns an error
     * @throws IOException       if there's a network error
     */
    public SlackUserDto getUser(String slackUserId) {
        try {
            UsersInfoResponse usersInfoResponse = slack.methods(slackBotToken).usersInfo(UsersInfoRequest.builder().user(slackUserId).build());
            if (!usersInfoResponse.isOk()) {
                log.error("Failed to retrieve user from Slack: {}", usersInfoResponse.getError());
                throw new RuntimeException("Failed to retrieve user: " + usersInfoResponse.getError());
            }
            return SlackUserConverter.convert(usersInfoResponse.getUser());

        } catch (SlackApiException e) {
            log.error("Slack API error while retrieving user: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("IO error while retrieving user from Slack: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Open view in conversation
     *
     * @return
     * @throws SlackApiException if Slack API returns an error
     * @throws IOException       if there's a network error
     */
    public void openCheckInAnswersView(String triggerId, View view) {
        try {
            ViewsOpenResponse response = slack.methods(slackBotToken).viewsOpen(
                    ViewsOpenRequest.builder()
                            .triggerId(triggerId)
                            .view(view)
                            .build()
            );
            if (!response.isOk()) {
                log.error("Failed to open view: {}", response.getError());
                throw new RuntimeException("Failed to open view: " + response.getError());
            }
            log.info(response.toString());
        } catch (SlackApiException e) {
            log.error("Slack API error while opening view: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("IO error while opening view: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Send webhook back to conversation
     *
     * @return
     * @throws SlackApiException if Slack API returns an error
     * @throws IOException       if there's a network error
     */
    public void sendActionResponse(String userId, String responseUrl, String outroMessage) {
        log.info("Sending ephemeral message to user {}", userId);

        List<LayoutBlock> blocks = List.of(
                Blocks.section(section -> section.text(BlockCompositions.markdownText(outroMessage))),
                Blocks.actions(actions -> actions
                        .elements(List.of(
                                BlockElements.button(b -> b
                                        .text(BlockCompositions.plainText("Edit"))
                                        .actionId(FINISH_CHECK_IN.name())
                                        .value("finish"))
                        ))
                )
        );
        try {
            actionResponseSender.send(responseUrl, ActionResponse.builder()
                    .replaceOriginal(true)
                    .blocks(blocks)
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send message to conversation
     *
     * @return
     * @throws SlackApiException if Slack API returns an error
     * @throws IOException       if there's a network error
     */
    public void sendChatPostMessage(String channelId, List<LayoutBlock> blocks, Message.Metadata metadata) {
        try {
            ChatPostMessageResponse response = slack.methods(slackBotToken).chatPostMessage(
                    ChatPostMessageRequest.builder()
                            .channel(channelId)
                            .metadata(metadata)
                            .blocks(blocks)
                            .build()
            );
            if (!response.isOk()) {
                throw new RuntimeException("Failed to send interactive message: " + response.getError());
            }
        } catch (SlackApiException e) {
            log.error("Slack API error while sending chat post message: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("IO error while sending chat post message: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<User> getAllUsers() throws SlackApiException, IOException {
        log.info("Retrieving all users from Slack workspace");

        if (slackBotToken == null || slackBotToken.trim().isEmpty()) {
            log.warn("Slack bot token is not configured");
            throw new IllegalStateException("Slack bot token is not configured");
        }

        MethodsClient methods = slack.methods(slackBotToken);

        try {
            UsersListResponse response = methods.usersList(req -> req
                    .limit(1000)
                    .includeLocale(true)
            );

            if (!response.isOk()) {
                log.error("Failed to retrieve users from Slack: {}", response.getError());
                throw new RuntimeException("Failed to retrieve users: " + response.getError());
            }

            List<User> users = response.getMembers();
            log.info("Successfully retrieved {} users from Slack workspace", users.size());

            return users;

        } catch (SlackApiException e) {
            log.error("Slack API error while retrieving users: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("IO error while retrieving users from Slack: {}", e.getMessage(), e);
            throw e;
        }
    }
}