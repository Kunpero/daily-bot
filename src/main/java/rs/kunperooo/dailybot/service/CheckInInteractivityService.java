package rs.kunperooo.dailybot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.kunperooo.dailybot.controller.dto.CheckInData;
import rs.kunperooo.dailybot.entity.CheckInHistoryEntity;
import rs.kunperooo.dailybot.service.dto.SaveAnswersDto;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInInteractivityService {
    private final SlackApiService slackApiService;
    private final CheckInService checkInService;

    public void openCheckInAnswersView(String triggerId, String userId, String responseUrl, String checkInHistoryUuid) {
        log.info("Sending check in submit form to Slack");

        Optional<CheckInHistoryEntity> checkIn = checkInService.findHistoryByUuid(UUID.fromString(checkInHistoryUuid));
        slackApiService.openCheckInAnswersView(triggerId, userId, responseUrl, checkIn.get());
    }

    public void saveSubmittedForm(SaveAnswersDto dto) {
        log.info("Saving submitted check in form");
        Optional<CheckInData> checkIn = checkInService.findByUuid(dto.getCheckInUuid());
        checkInService.saveOrUpdateAnswers(dto);

        slackApiService.sendActionResponse(dto.getUserId(), dto.getResponseUrl(), checkIn.get().getOutroMessage());
    }
}
