package rs.kunperooo.dailybot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunperooo.dailybot.entity.CheckInEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckInEntity, Long> {

    Optional<CheckInEntity> findByUuid(UUID uuid);
    List<CheckInEntity> findByOwner(String owner);
    Page<CheckInEntity> findByOwner(String owner, Pageable pageable);
    Optional<CheckInEntity> findByUuidAndOwner(UUID uuid, String owner);
    boolean existsByUuidAndOwner(UUID uuid, String owner);
    void deleteByUuid(UUID uuid);
}