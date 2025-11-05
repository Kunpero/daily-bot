package rs.kunperooo.dailybot.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for CheckIn questions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRest {

    private UUID uuid;

    private String text;

    private Integer order;
}