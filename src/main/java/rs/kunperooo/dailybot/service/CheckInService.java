package rs.kunperooo.dailybot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInService {
    void createCheckIn(String owner, String name, String introMessage, String outroMessage);

    List<CheckInRestData> findByOwner(String owner);

    List<CheckInRestData> findAll();

    Optional<CheckInRestData> findByUuid(UUID uuid);

    Page<CheckInRestData> findByOwner(String owner, Pageable pageable);

    void updateCheckInMessages(UUID uuid, String owner, String name, String introMessage, String outroMessage);

    void deleteCheckIn(UUID uuid, String owner);
}
