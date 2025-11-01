package rs.kunperooo.dailybot.controller.dto;

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

    private List<QuestionDto> questions;

    private List<MemberDto> members;

    private Schedule schedule;
}