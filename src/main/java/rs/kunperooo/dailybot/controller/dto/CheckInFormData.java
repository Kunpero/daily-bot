package rs.kunperooo.dailybot.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.kunperooo.dailybot.service.dto.MemberDto;
import rs.kunperooo.dailybot.service.dto.QuestionDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInFormData {

    private String name;

    private String introMessage;

    private String outroMessage;

    private List<QuestionRest> questions;

    private List<MemberRest> members;

    private ScheduleRest schedule;
}