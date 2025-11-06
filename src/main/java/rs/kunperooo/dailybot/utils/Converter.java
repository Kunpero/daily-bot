package rs.kunperooo.dailybot.utils;

import org.springframework.data.domain.Page;
import rs.kunperooo.dailybot.controller.dto.CheckInDataRest;
import rs.kunperooo.dailybot.controller.dto.MemberRest;
import rs.kunperooo.dailybot.controller.dto.QuestionRest;
import rs.kunperooo.dailybot.controller.dto.ScheduleRest;
import rs.kunperooo.dailybot.controller.dto.SlackUserRest;
import rs.kunperooo.dailybot.service.dto.CheckInDataDto;
import rs.kunperooo.dailybot.service.dto.MemberDto;
import rs.kunperooo.dailybot.service.dto.QuestionDto;
import rs.kunperooo.dailybot.service.dto.ScheduleDto;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.entity.CheckInNotificationScheduleEntity;
import rs.kunperooo.dailybot.entity.CheckInQuestionEntity;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Converter {

    public static List<CheckInDataDto> convertToDtoList(List<CheckInEntity> checkInList) {
        return checkInList.stream()
                .map(Converter::convertToDto)
                .toList();
    }

    public static Optional<CheckInDataDto> convertToDto(Optional<CheckInEntity> checkIn) {
        return checkIn.map(Converter::convertToDto);
    }

    public static CheckInDataDto convertToDto(CheckInEntity checkIn) {
        return CheckInDataDto.builder()
                .uuid(checkIn.getUuid())
                .owner(checkIn.getOwner())
                .name(checkIn.getName())
                .lastUpdateDate(checkIn.getLastUpdateDate())
                .creationDate(checkIn.getCreationDate())
                .introMessage(checkIn.getIntroMessage())
                .outroMessage(checkIn.getOutroMessage())
                .questions(convertToQuestionsDtoList(checkIn.getCheckInQuestions()))
                .members(convertToMembersDtoList(checkIn.getMembers()))
                .schedule(convertToDto(checkIn.getNotificationSchedule()))
                .build();
    }

    public static ScheduleDto convertToDto(CheckInNotificationScheduleEntity schedule) {
        return ScheduleDto.builder()
                .startDate(schedule.getStartDate())
                .time(schedule.getTime())
                .timezone(schedule.getTimezone())
                .frequency(schedule.getFrequency())
                .days(schedule.getWeekDays())
                .build();
    }

    public static List<QuestionDto> convertToQuestionsDtoList(List<CheckInQuestionEntity> questions) {
        return questions.stream()
                .map(Converter::convertToDto)
                .sorted(Comparator.comparing(QuestionDto::getOrder))
                .toList();
    }

    public static QuestionDto convertToDto(CheckInQuestionEntity question) {
        return QuestionDto.builder()
                .uuid(question.getUuid())
                .text(question.getQuestion())
                .order(question.getOrderNumber())
                .build();
    }

    public static CheckInQuestionEntity convertToEntity(QuestionDto question) {
        return CheckInQuestionEntity.builder()
                .uuid(question.getUuid())
                .question(question.getText())
                .orderNumber(question.getOrder())
                .build();
    }

    public static Page<CheckInDataDto> convertToDtoPage(Page<CheckInEntity> checkInPage) {
        return checkInPage.map(Converter::convertToDto);
    }

    public static List<CheckInQuestionEntity> convertToEntityList(List<QuestionDto> questions) {
        if (questions.isEmpty()) {
            return new LinkedList<>();
        }
        return questions.stream()
                .map(Converter::convertToEntity)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static List<MemberDto> convertToMembersDtoList(List<rs.kunperooo.dailybot.entity.Member> members) {
        if (members == null || members.isEmpty()) {
            return new LinkedList<>();
        }
        return members.stream()
                .map(Converter::convertToDto)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static MemberDto convertToDto(rs.kunperooo.dailybot.entity.Member member) {
        return MemberDto.builder()
                .id(member.getUsername())
                .realName(member.getRealName())
                .imageUrl(member.getImageUrl())
                .build();
    }

    public static List<rs.kunperooo.dailybot.entity.Member> convertToMemberEntityList(List<MemberDto> members) {
        if (members == null || members.isEmpty()) {
            return new LinkedList<>();
        }
        return members.stream()
                .map(Converter::convertToEntity)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static rs.kunperooo.dailybot.entity.Member convertToEntity(MemberDto member) {
        return rs.kunperooo.dailybot.entity.Member.builder()
                .username(member.getId())
                .realName(member.getRealName())
                .imageUrl(member.getImageUrl())
                .build();
    }

    public static CheckInDataRest convert(CheckInDataDto checkInDataDto) {
        return CheckInDataRest.builder()
                .uuid(checkInDataDto.getUuid())
                .owner(checkInDataDto.getOwner())
                .name(checkInDataDto.getName())
                .lastUpdateDate(checkInDataDto.getLastUpdateDate())
                .creationDate(checkInDataDto.getCreationDate())
                .introMessage(checkInDataDto.getIntroMessage())
                .outroMessage(checkInDataDto.getOutroMessage())
                .questions(convertToQuestionListRest(checkInDataDto.getQuestions()))
                .members(convertToMemberListRest(checkInDataDto.getMembers()))
                .schedule(convertToRest(checkInDataDto.getSchedule()))
                .build();
    }

    public static Page<CheckInDataRest> convertToRest(Page<CheckInDataDto> checkInDataDtoPage) {
        return checkInDataDtoPage.map(Converter::convert);
    }

    public static List<CheckInDataRest> convertToRest(List<CheckInDataDto> checkInDataDtoList) {
        return checkInDataDtoList.stream()
                .map(Converter::convert)
                .toList();
    }

    public static Optional<CheckInDataRest> convertToRest(Optional<CheckInDataDto> checkInDataDto) {
        return checkInDataDto.map(Converter::convert);
    }

    public static List<QuestionRest> convertToQuestionListRest(List<QuestionDto> questions) {
        if (questions == null || questions.isEmpty()) {
            return new LinkedList<>();
        }
        return questions.stream()
                .map(Converter::convertToRest)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static QuestionRest convertToRest(QuestionDto questionDto) {
        return QuestionRest.builder()
                .uuid(questionDto.getUuid())
                .text(questionDto.getText())
                .order(questionDto.getOrder())
                .build();
    }

    public static List<MemberRest> convertToMemberListRest(List<MemberDto> members) {
        if (members == null || members.isEmpty()) {
            return new LinkedList<>();
        }
        return members.stream()
                .map(Converter::convertToRest)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static MemberRest convertToRest(MemberDto memberDto) {
        return MemberRest.builder()
                .id(memberDto.getId())
                .realName(memberDto.getRealName())
                .imageUrl(memberDto.getImageUrl())
                .build();
    }

    public static ScheduleRest convertToRest(ScheduleDto scheduleDto) {
        if (scheduleDto == null) {
            return null;
        }
        return ScheduleRest.builder()
                .startDate(scheduleDto.getStartDate())
                .time(scheduleDto.getTime())
                .timezone(scheduleDto.getTimezone() != null ? ZoneId.of(scheduleDto.getTimezone()) : null)
                .frequency(scheduleDto.getFrequency())
                .days(scheduleDto.getDays())
                .build();
    }

    public static ScheduleDto convertToDto(ScheduleRest scheduleRest) {
        if (scheduleRest == null) {
            return null;
        }
        return ScheduleDto.builder()
                .startDate(scheduleRest.getStartDate())
                .time(scheduleRest.getTime())
                .timezone(scheduleRest.getTimezone() != null ? scheduleRest.getTimezone().getId() : null)
                .frequency(scheduleRest.getFrequency())
                .days(scheduleRest.getDays())
                .build();
    }

    public static List<QuestionDto> convertToQuestionListDto(List<QuestionRest> questions) {
        if (questions == null || questions.isEmpty()) {
            return new LinkedList<>();
        }
        return questions.stream()
                .map(Converter::convertToDto)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static QuestionDto convertToDto(QuestionRest questionRest) {
        return QuestionDto.builder()
                .uuid(questionRest.getUuid())
                .text(questionRest.getText())
                .order(questionRest.getOrder())
                .build();
    }

    public static List<MemberDto> convertToMemberListDto(List<MemberRest> members) {
        if (members == null || members.isEmpty()) {
            return new LinkedList<>();
        }
        return members.stream()
                .map(Converter::convertToDto)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }

    public static MemberDto convertToDto(MemberRest memberRest) {
        return MemberDto.builder()
                .id(memberRest.getId())
                .realName(memberRest.getRealName())
                .imageUrl(memberRest.getImageUrl())
                .build();
    }

    public static SlackUserRest convertToRest(SlackUserDto slackUserDto) {
        return SlackUserRest.builder()
                .id(slackUserDto.getId())
                .name(slackUserDto.getName())
                .realName(slackUserDto.getRealName())
                .email(slackUserDto.getEmail())
                .phone(slackUserDto.getPhone())
                .title(slackUserDto.getTitle())
                .timezone(slackUserDto.getTimezone())
                .timezoneLabel(slackUserDto.getTimezoneLabel())
                .locale(slackUserDto.getLocale())
                .teamId(slackUserDto.getTeamId())
                .profileImage24(slackUserDto.getProfileImage24())
                .profileImage32(slackUserDto.getProfileImage32())
                .profileImage48(slackUserDto.getProfileImage48())
                .profileImage72(slackUserDto.getProfileImage72())
                .profileImage192(slackUserDto.getProfileImage192())
                .profileImage512(slackUserDto.getProfileImage512())
                .build();
    }

    public static List<SlackUserRest> convertToListRest(List<SlackUserDto> slackUserDtoList) {
        if (slackUserDtoList == null || slackUserDtoList.isEmpty()) {
            return new LinkedList<>();
        }
        return slackUserDtoList.stream()
                .map(Converter::convertToRest)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
    }
}
