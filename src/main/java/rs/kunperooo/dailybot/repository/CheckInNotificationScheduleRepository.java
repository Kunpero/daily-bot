package rs.kunperooo.dailybot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunperooo.dailybot.entity.CheckInNotificationScheduleEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInNotificationScheduleRepository extends JpaRepository<CheckInNotificationScheduleEntity, Long> {

    List<CheckInNotificationScheduleEntity> findByCheckInId(Long checkInId);

    Optional<CheckInNotificationScheduleEntity> findByIdAndCheckInId(Long id, Long checkInId);

    void deleteByCheckInId(Long checkInId);

    List<CheckInNotificationScheduleEntity> findByNextExecutionAtIsNotNull();
}
