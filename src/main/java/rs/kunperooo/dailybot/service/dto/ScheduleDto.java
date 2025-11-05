package rs.kunperooo.dailybot.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.kunperooo.dailybot.entity.Frequency;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private LocalTime time;
    private String timezone;
    private Frequency frequency;
    private List<DayOfWeek> days;
}
