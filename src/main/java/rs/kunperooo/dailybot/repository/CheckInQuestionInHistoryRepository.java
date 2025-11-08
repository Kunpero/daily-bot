package rs.kunperooo.dailybot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunperooo.dailybot.entity.CheckInQuestionInHistoryEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckInQuestionInHistoryRepository extends JpaRepository<CheckInQuestionInHistoryEntity, Long> {
    CheckInQuestionInHistoryEntity findByUuid(UUID uuid);

    List<CheckInQuestionInHistoryEntity> findByCheckInHistoryUuid(UUID checkInHistoryUuid);
}
