package rs.kunperooo.dailybot.service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CheckInDataDto {
    private UUID uuid;

    private String owner;

    private String name;

    private LocalDateTime creationDate;

    private LocalDateTime lastUpdateDate;

    private String introMessage;

    private String outroMessage;

    private List<QuestionDto> questions;

    private List<MemberDto> members;

    private ScheduleDto schedule;
}
