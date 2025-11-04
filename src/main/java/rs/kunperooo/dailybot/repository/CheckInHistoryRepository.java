package rs.kunperooo.dailybot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunperooo.dailybot.entity.CheckInHistoryEntity;

@Repository
public interface CheckInHistoryRepository extends JpaRepository<CheckInHistoryEntity, Long> {
}
