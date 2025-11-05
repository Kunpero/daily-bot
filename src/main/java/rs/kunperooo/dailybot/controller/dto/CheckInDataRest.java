package rs.kunperooo.dailybot.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CheckInDataRest {
    private UUID uuid;

    private String owner;

    private String name;

    private LocalDateTime creationDate;

    private LocalDateTime lastUpdateDate;

    private String introMessage;

    private String outroMessage;

    private List<QuestionRest> questions;

    private List<MemberRest> members;

    private ScheduleRest schedule;
}
