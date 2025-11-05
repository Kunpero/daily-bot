package rs.kunperooo.dailybot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.kunperooo.dailybot.service.dto.CheckInDataDto;
import rs.kunperooo.dailybot.service.dto.MemberDto;
import rs.kunperooo.dailybot.service.dto.QuestionDto;
import rs.kunperooo.dailybot.controller.dto.ScheduleRest;
import rs.kunperooo.dailybot.entity.CheckInHistoryEntity;
import rs.kunperooo.dailybot.service.dto.SaveAnswersDto;
import rs.kunperooo.dailybot.service.dto.ScheduleDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInService {
    void createCheckIn(String owner, String name, String introMessage, String outroMessage, List<QuestionDto> questions, List<MemberDto> members, ScheduleDto schedule);

    List<CheckInDataDto> findByOwner(String owner);

    List<CheckInDataDto> findAll();

    Optional<CheckInDataDto> findByUuid(UUID uuid);

    Page<CheckInDataDto> findByOwner(String owner, Pageable pageable);

    void updateCheckIn(UUID uuid, String owner, String name, String introMessage, String outroMessage, List<QuestionDto> questions, List<MemberDto> members, ScheduleDto schedule);

    void deleteCheckIn(UUID uuid, String owner);

    void saveOrUpdateAnswers(SaveAnswersDto dto);

    Optional<CheckInHistoryEntity> findHistoryByUuid(UUID uuid);

    List<CheckInDataDto> findByNextExecutionIsBefore(ZonedDateTime nextExecutionBefore, Pageable pageable);

    UUID saveHistory(CheckInDataDto checkIn);

    void saveNextExecution(UUID checkInUuid, ZonedDateTime nextExecution);
}
