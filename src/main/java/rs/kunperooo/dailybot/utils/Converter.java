package rs.kunperooo.dailybot.utils;

import org.springframework.data.domain.Page;
import rs.kunperooo.dailybot.controller.dto.CheckInData;
import rs.kunperooo.dailybot.controller.dto.MemberDto;
import rs.kunperooo.dailybot.controller.dto.QuestionDto;
import rs.kunperooo.dailybot.controller.dto.RestSchedule;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.entity.CheckInNotificationScheduleEntity;
import rs.kunperooo.dailybot.entity.CheckInQuestionEntity;
import rs.kunperooo.dailybot.entity.Member;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Converter {

    public static List<CheckInData> convertCheckIns(List<CheckInEntity> checkInList) {
        return checkInList.stream()
                .map(Converter::convert)
                .toList();
    }

    public static Optional<CheckInData> convert(Optional<CheckInEntity> checkIn) {
        return checkIn.map(Converter::convert);
    }

    public static CheckInData convert(CheckInEntity checkIn) {
        return CheckInData.builder()
                .uuid(checkIn.getUuid())
                .owner(checkIn.getOwner())
                .name(checkIn.getName())
                .lastUpdateDate(checkIn.getLastUpdateDate())
                .creationDate(checkIn.getCreationDate())
                .introMessage(checkIn.getIntroMessage())
                .outroMessage(checkIn.getOutroMessage())
                .questions(convertQuestions(checkIn.getCheckInQuestions()))
                .members(convertMembers(checkIn.getMembers()))
                .schedule(convert(checkIn.getNotificationSchedule()))
                .build();
    }

    public static RestSchedule convert(CheckInNotificationScheduleEntity schedule) {
        return RestSchedule.builder()
                .startDate(schedule.getStartDate())
                .time(schedule.getTime())
                .timezone(schedule.getTimezone())
                .frequency(schedule.getFrequency())
                .days(schedule.getWeekDays())
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

    public static Page<CheckInData> convert(Page<CheckInEntity> checkInPage) {
        return checkInPage.map(Converter::convert);
    }

    public static List<CheckInQuestionEntity> convert(List<QuestionDto> questions) {
        if (questions.isEmpty()) {
            return new LinkedList<>();
        }
        return questions.stream()
                .map(Converter::convert)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static List<MemberDto> convertMembers(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return new LinkedList<>();
        }
        return members.stream()
                .map(Converter::convert)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static MemberDto convert(Member member) {
        return MemberDto.builder()
                .id(member.getUsername())
                .realName(member.getRealName())
                .imageUrl(member.getImageUrl())
                .build();
    }

    public static List<Member> convertMemberDtos(List<MemberDto> members) {
        if (members == null || members.isEmpty()) {
            return new LinkedList<>();
        }
        return members.stream()
                .map(Converter::convert)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static Member convert(MemberDto member) {
        return Member.builder()
                .username(member.getId())
                .realName(member.getRealName())
                .imageUrl(member.getImageUrl())
                .build();
    }
}
