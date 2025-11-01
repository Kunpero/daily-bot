package rs.kunperooo.dailybot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.controller.dto.MemberDto;
import rs.kunperooo.dailybot.controller.dto.QuestionDto;
import rs.kunperooo.dailybot.controller.dto.Schedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInService {
    void createCheckIn(String owner, String name, String introMessage, String outroMessage, List<QuestionDto> questions, List<MemberDto> members, Schedule schedule);

    List<CheckInRestData> findByOwner(String owner);

    List<CheckInRestData> findAll();

    Optional<CheckInRestData> findByUuid(UUID uuid);

    Page<CheckInRestData> findByOwner(String owner, Pageable pageable);

    void updateCheckIn(UUID uuid, String owner, String name, String introMessage, String outroMessage, List<QuestionDto> questions, List<MemberDto> members, Schedule schedule);

    void deleteCheckIn(UUID uuid, String owner);
}
