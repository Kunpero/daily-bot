package rs.kunperooo.dailybot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.kunperooo.dailybot.controller.dto.form.CheckInSubmissionMeta;
import rs.kunperooo.dailybot.entity.CheckInAnswerEntity;
import rs.kunperooo.dailybot.service.dto.CheckInDataDto;
import rs.kunperooo.dailybot.entity.CheckInHistoryEntity;
import rs.kunperooo.dailybot.service.dto.SaveAnswersDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static rs.kunperooo.dailybot.service.SlackApiService.PLAIN_TEXT_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInInteractivityService {
    private final SlackApiService slackApiService;
    private final CheckInService checkInService;
    private final ObjectMapper objectMapper;

    public void openCheckInAnswersView(String triggerId, String userId, String responseUrl, String checkInHistoryUuid) {
        log.info("Sending check in submit form to Slack");

        Optional<CheckInHistoryEntity> history = checkInService.findHistoryByUuid(UUID.fromString(checkInHistoryUuid));
        try {
            String privateMetadata = objectMapper.writeValueAsString(CheckInSubmissionMeta.builder()
                    .responseUrl(responseUrl)
                    .historyUuid(history.get().getCheckIn().getUuid())
                    .build());
            slackApiService.openCheckInAnswersView(triggerId, buildCheckInView(userId, privateMetadata, history.get()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveSubmittedForm(SaveAnswersDto dto) {
        log.info("Saving submitted check in form");
        Optional<CheckInDataDto> checkIn = checkInService.findByUuid(dto.getCheckInUuid());
        checkInService.saveOrUpdateAnswers(dto);

        slackApiService.sendActionResponse(dto.getUserId(), dto.getResponseUrl(), checkIn.get().getOutroMessage());
    }

    private static View buildCheckInView(String userId, String metaData, CheckInHistoryEntity history) {
        List<LayoutBlock> inputBlocks = history.getCheckInQuestionInHistory().stream()
                .map(q ->
                        Blocks.input(input -> input
                                .blockId(q.getUuid().toString())
                                .element(BlockElements.plainTextInput(pi -> {
                                    Optional<CheckInAnswerEntity> answer = q.getCheckInAnswers().stream()
                                            .filter(a -> userId.equals(a.getUserId()))
                                            .findFirst();
                                    String answerUuid = answer.isPresent() ? answer.get().getUuid().toString() : UUID.randomUUID().toString();
                                    pi.actionId(answerUuid);
                                    pi.initialValue(answer.isPresent() ? answer.get().getAnswer() : "");
                                    return pi;
                                }))
                                .label(PlainTextObject.builder().text(q.getCheckInQuestion().getQuestion()).build())
                        )
                )
                .collect(Collectors.toList());

        return View.builder()
                .type("modal")
                .callbackId("check_in_survey_submission")
                .title(ViewTitle.builder().type(PLAIN_TEXT_TYPE).text(history.getCheckIn().getName()).build())
                .submit(ViewSubmit.builder().type(PLAIN_TEXT_TYPE).text("Submit").build())
                .close(ViewClose.builder().type(PLAIN_TEXT_TYPE).text("Cancel").build())
                .blocks(inputBlocks)
                .privateMetadata(metaData)
                .build();

    }
}
