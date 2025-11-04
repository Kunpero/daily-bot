package rs.kunperooo.dailybot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.slack.api.app_backend.dialogs.payload.PayloadTypeDetector;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.model.view.ViewState;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.kunperooo.dailybot.controller.dto.CheckInSubmissionMeta;
import rs.kunperooo.dailybot.service.CheckInInteractivityService;
import rs.kunperooo.dailybot.service.dto.AnswerDto;
import rs.kunperooo.dailybot.service.dto.SaveAnswersDto;
import rs.kunperooo.dailybot.utils.ActionId;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class InteractivityHandlerController {
    private static final PayloadTypeDetector TYPE_DETECTOR = new PayloadTypeDetector();
    private static final String BLOCK_ACTIONS_TYPE = "block_actions";
    private static final String VIEW_SUBMISSION_TYPE = "view_submission";

    private final CheckInInteractivityService checkInInteractivityService;
    private final Gson gson;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @RequestMapping("/interactivity")
    public ResponseEntity<Void> interactivity(HttpServletRequest request) {
        String body = request.getReader().lines().collect(Collectors.joining());
        body = URLDecoder.decode(body, StandardCharsets.UTF_8).replaceFirst("payload=", "");

        String type = TYPE_DETECTOR.detectType(body);
        if (BLOCK_ACTIONS_TYPE.equals(type)) {
            handleBlockActions(body);
        } else if (VIEW_SUBMISSION_TYPE.equals(type)) {
            handleViewSubmission(body);
        }
        return ResponseEntity.ok().build();
    }

    private void handleBlockActions(String body) {
        BlockActionPayload payload = gson.fromJson(body, BlockActionPayload.class);
        List<BlockActionPayload.Action> actions = payload.getActions();
        ActionId actionId = ActionId.safeValueOf(actions.get(0).getActionId());

        if (actionId == ActionId.START_CHECK_IN) {
            checkInInteractivityService.openCheckInAnswersView(payload.getTriggerId(), payload.getUser().getId(), payload.getResponseUrl(), payload.getMessage().getMetadata().getEventPayload().get("checkInHistoryUuid").toString());
        } else if (actionId == ActionId.FINISH_CHECK_IN) {
            checkInInteractivityService.openCheckInAnswersView(payload.getTriggerId(), payload.getUser().getId(), payload.getResponseUrl(), payload.getMessage().getMetadata().getEventPayload().get("checkInHistoryUuid").toString());
        }
    }

    private void handleViewSubmission(String body) {
        ViewSubmissionPayload payload = gson.fromJson(body, ViewSubmissionPayload.class);
        checkInInteractivityService.saveSubmittedForm(buildAnswerDto(payload));
    }

    @SneakyThrows
    private SaveAnswersDto buildAnswerDto(ViewSubmissionPayload payload) {
        Map<String, Map<String, ViewState.Value>> submittedValues = payload.getView().getState().getValues();

        List<AnswerDto> answers = new LinkedList<>();

        for (Map.Entry<String, Map<String, ViewState.Value>> entry : submittedValues.entrySet()) {
            AnswerDto answer = AnswerDto.builder()
                    .questionInHistoryUuid(UUID.fromString(entry.getKey()))
                    .uuid(UUID.fromString(entry.getValue().entrySet().stream().findFirst().get().getKey()))
                    .answer(entry.getValue().entrySet().stream().findFirst().get().getValue().getValue()).build();
            answers.add(answer);
        }

        CheckInSubmissionMeta meta = objectMapper.readValue(payload.getView().getPrivateMetadata(), CheckInSubmissionMeta.class);
        return SaveAnswersDto.builder()
                .userId(payload.getUser().getId())
                .checkInUuid(meta.getHistoryUuid())
                .responseUrl(meta.getResponseUrl())
                .answers(answers)
                .build();
    }
}
