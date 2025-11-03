package rs.kunperooo.dailybot.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;
import rs.kunperooo.dailybot.utils.SlackUserConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static rs.kunperooo.dailybot.utils.ActionId.START_CHECK_IN;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackApiService {

    public static final String PLAIN_TEXT_TYPE = "plain_text";
    @Value("${slack.bot.token:}")
    private String slackBotToken;

    private final Slack slack;

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

    @SneakyThrows
    public void openCheckInAnswersView(String triggerId, CheckInRestData checkIn) {
        List<LayoutBlock> inputBlocks = checkIn.getQuestions().stream()
                .map(q ->
                        Blocks.input(input -> input
                                .blockId(q.getUuid().toString())
                                .element(BlockElements.plainTextInput(pi -> pi.actionId("answer_" + q.hashCode())))
                                .label(PlainTextObject.builder().text(q.getText()).build())
                        )
                )
                .collect(Collectors.toList());

        var view = View.builder()
                .type("modal")
                .callbackId("survey_submission")
                .title(ViewTitle.builder().type(PLAIN_TEXT_TYPE).text(checkIn.getName()).build())
                .submit(ViewSubmit.builder().type(PLAIN_TEXT_TYPE).text("Submit").build())
                .close(ViewClose.builder().type(PLAIN_TEXT_TYPE).text("Cancel").build())
                .blocks(inputBlocks)
                .privateMetadata(checkIn.getUuid().toString())
                .build();
        ViewsOpenResponse response = slack.methods(slackBotToken).viewsOpen(
                ViewsOpenRequest.builder()
                        .triggerId(triggerId)
                        .view(view)
                        .build()
        );
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
                    .limit(1000) // Maximum allowed by Slack API
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