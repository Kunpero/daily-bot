package rs.kunperooo.dailybot.controller.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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