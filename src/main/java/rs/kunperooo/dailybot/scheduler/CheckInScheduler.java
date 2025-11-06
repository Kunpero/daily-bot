package rs.kunperooo.dailybot.scheduler;

import com.slack.api.model.Message;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.kunperooo.dailybot.service.dto.CheckInDataDto;
import rs.kunperooo.dailybot.service.dto.MemberDto;
import rs.kunperooo.dailybot.service.CheckInService;
import rs.kunperooo.dailybot.service.SlackApiService;
import rs.kunperooo.dailybot.utils.ScheduleUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static rs.kunperooo.dailybot.utils.ActionId.START_CHECK_IN;

@Component
@RequiredArgsConstructor
public class CheckInScheduler {
    private final CheckInService checkInService;
    private final SlackApiService slackApiService;

    @Scheduled(cron = "${check.in.cron}")
    public void schedule() {
        List<CheckInDataDto> checkIns = checkInService.findByNextExecutionIsBefore(ZonedDateTime.now(ZoneId.systemDefault()), Pageable.ofSize(10));

        for (CheckInDataDto checkIn : checkIns) {
            sendNotification(checkIn);
            checkInService.saveNextExecution(checkIn.getUuid(), ScheduleUtils.calculateNextExecution(checkIn.getSchedule()));
        }
    }

    private void sendNotification(CheckInDataDto checkIn) {
        UUID historyUuid = checkInService.saveHistory(checkIn);

        List<LayoutBlock> blocks = List.of(
                Blocks.section(section -> section.text(BlockCompositions.markdownText(checkIn.getIntroMessage()))),
                Blocks.actions(actions -> actions
                        .elements(List.of(
                                BlockElements.button(b -> b
                                        .text(BlockCompositions.plainText("Yes"))
                                        .actionId(START_CHECK_IN.name())
                                        .value("start"))
                        ))
                )
        );
        Message.Metadata metadata = Message.Metadata.builder()
                .eventType("notify_user_with_check_in")
                .eventPayload(Map.of("checkInHistoryUuid", historyUuid.toString()))
                .build();
        for (MemberDto member : checkIn.getMembers()) {
            slackApiService.sendChatPostMessage(member.getId(), blocks, metadata);
        }
    }
}
