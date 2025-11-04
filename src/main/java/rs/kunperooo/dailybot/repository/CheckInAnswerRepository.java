package rs.kunperooo.dailybot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunperooo.dailybot.entity.CheckInAnswerEntity;

import java.util.UUID;

@Repository
public interface CheckInAnswerRepository extends JpaRepository<CheckInAnswerEntity, Long> {
    CheckInAnswerEntity findByUuid(UUID uuid);
}
