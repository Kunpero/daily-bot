package rs.kunperooo.dailybot.utils;

import org.springframework.data.domain.Page;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.controller.dto.QuestionDto;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.entity.CheckInQuestionEntity;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Converter {

    public static List<CheckInRestData> convertCheckIns(List<CheckInEntity> checkInList) {
        return checkInList.stream()
                .map(Converter::convert)
                .toList();
    }

    public static Optional<CheckInRestData> convert(Optional<CheckInEntity> checkIn) {
        return checkIn.map(Converter::convert);
    }

    public static CheckInRestData convert(CheckInEntity checkIn) {
        return CheckInRestData.builder()
                .uuid(checkIn.getUuid())
                .owner(checkIn.getOwner())
                .name(checkIn.getName())
                .lastUpdateDate(checkIn.getLastUpdateDate())
                .creationDate(checkIn.getCreationDate())
                .introMessage(checkIn.getIntroMessage())
                .outroMessage(checkIn.getOutroMessage())
                .questions(convertQuestions(checkIn.getCheckInQuestions()))
                .build();
    }

    public static List<QuestionDto> convertQuestions(List<CheckInQuestionEntity> questions) {
        return questions.stream()
                .map(Converter::convert)
                .sorted(Comparator.comparing(QuestionDto::getOrder))
                .toList();
    }

    public static QuestionDto convert(CheckInQuestionEntity question) {
        return QuestionDto.builder()
                .uuid(question.getUuid())
                .text(question.getQuestion())
                .order(question.getOrderNumber())
                .build();
    }

    public static CheckInQuestionEntity convert(QuestionDto question) {
        return CheckInQuestionEntity.builder()
                .uuid(question.getUuid())
                .question(question.getText())
                .orderNumber(question.getOrder())
                .build();
    }

    public static Page<CheckInRestData> convert(Page<CheckInEntity> checkInPage) {
        return checkInPage.map(Converter::convert);
    }

    public static List<CheckInQuestionEntity> convert(List<QuestionDto> questions) {
        if (questions.isEmpty()) {
            return new LinkedList<>();
        }
        return questions.stream()
                .map(Converter::convert)
                .toList();
    }
}
