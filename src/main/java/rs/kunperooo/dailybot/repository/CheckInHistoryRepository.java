package rs.kunperooo.dailybot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunperooo.dailybot.entity.CheckInHistoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInHistoryRepository extends JpaRepository<CheckInHistoryEntity, Long> {
    Optional<CheckInHistoryEntity> findByUuid(UUID uuid);

    List<CheckInHistoryEntity> findByCheckInUuid(UUID checkInUuid);
}
