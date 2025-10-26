package rs.kunperooo.dailybot.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInFormData {

    private String name;

    private String introMessage;

    private String outroMessage;

    private List<QuestionDto> questions = new LinkedList<>();

    private List<MemberDto> members = new ArrayList<>();
}