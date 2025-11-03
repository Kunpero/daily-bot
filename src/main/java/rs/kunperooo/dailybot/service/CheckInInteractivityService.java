package rs.kunperooo.dailybot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInInteractivityService {
    private final SlackApiService slackApiService;
    private final CheckInService checkInService;

    public void openCheckInAnswersView(String triggerId, String checkInUuid) {
        log.info("Sending check in submit form to Slack");

        Optional<CheckInRestData> checkIn = checkInService.findByUuid(UUID.fromString(checkInUuid));
        slackApiService.openCheckInAnswersView(triggerId, checkIn.get());
    }

/*    todo
public void saveSubmittedForm(String checkInUuid, String answer) {
        log.info("Saving submitted check in form");

        CheckInRestData chechIn = checkInService.findByUuid(UUID.fromString(checkInUuid)).get();

        checkInService.upda

    }*/
}
