package rs.kunperooo.dailybot.service.dto.history;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CheckInHistoryDto {
    private LocalDate creationDate;
    private List<ShowQuestionDto> questions;
}
