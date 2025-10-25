package rs.kunperooo.dailybot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.repository.CheckInRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static rs.kunperooo.dailybot.utils.Converter.convert;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RelationalDbCheckInService implements CheckInService {

    private final CheckInRepository checkInRepository;

    public void createCheckIn(String owner, String name, String introMessage, String outroMessage) {
        log.info("Creating new check-in for owner: {} with name: {}", owner, name);

        CheckInEntity checkIn = CheckInEntity.builder()
                .uuid(java.util.UUID.randomUUID())
                .owner(owner)
                .name(name)
                .introMessage(introMessage)
                .outroMessage(outroMessage)
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        CheckInEntity savedCheckIn = checkInRepository.save(checkIn);
        log.info("Check-in created successfully with ID: {}", savedCheckIn.getId());
    }

    @Transactional(readOnly = true)
    public List<CheckInRestData> findByOwner(String owner) {
        log.debug("Finding all check-ins for owner: {}", owner);
        return convert(checkInRepository.findByOwner(owner));
    }

    @Transactional(readOnly = true)
    public List<CheckInRestData> findAll() {
        log.debug("Finding all check-ins");
        return convert(checkInRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<CheckInRestData> findByUuid(UUID uuid) {
        log.debug("Finding check-in by ID: {}", uuid);
        return convert(checkInRepository.findByUuid(uuid));
    }

    @Transactional(readOnly = true)
    public Page<CheckInRestData> findByOwner(String owner, Pageable pageable) {
        log.debug("Finding check-ins for owner: {} with pagination", owner);
        return convert(checkInRepository.findByOwner(owner, pageable));
    }

    public void updateCheckInMessages(UUID uuid, String owner, String name, String introMessage, String outroMessage) {
        log.info("Updating check-in for ID: {} and owner: {} with name: {}", uuid, owner, name);

        CheckInEntity checkIn = checkInRepository.findByUuidAndOwner(uuid, owner)
                .orElseThrow(() -> new RuntimeException("Check-in not found with ID: " + uuid + " for owner: " + owner));

        checkIn.setName(name);
        checkIn.setIntroMessage(introMessage);
        checkIn.setOutroMessage(outroMessage);
        checkIn.setLastUpdateDate(LocalDateTime.now());

        checkInRepository.save(checkIn);
    }

    public void deleteCheckIn(UUID uuid, String owner) {
        log.info("Deleting check-in with ID: {} for owner: {}", uuid, owner);

        if (!checkInRepository.existsByUuidAndOwner(uuid, owner)) {
            throw new RuntimeException("Check-in not found with ID: " + uuid + " for owner: " + owner);
        }

        checkInRepository.deleteByUuid(uuid);
        log.info("Check-in deleted successfully");
    }
}