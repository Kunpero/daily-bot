package rs.kunperooo.dailybot.controller.dto.history;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CheckInHistoryRest {
    private LocalDate creationDate;
    private List<ShowQuestionRest> questions;
}
