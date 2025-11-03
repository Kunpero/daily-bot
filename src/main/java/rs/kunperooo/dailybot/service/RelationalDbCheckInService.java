package rs.kunperooo.dailybot.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.controller.dto.MemberDto;
import rs.kunperooo.dailybot.controller.dto.QuestionDto;
import rs.kunperooo.dailybot.controller.dto.Schedule;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.entity.CheckInNotificationScheduleEntity;
import rs.kunperooo.dailybot.entity.CheckInQuestionEntity;
import rs.kunperooo.dailybot.repository.CheckInNotificationScheduleRepository;
import rs.kunperooo.dailybot.repository.CheckInQuestionRepository;
import rs.kunperooo.dailybot.repository.CheckInRepository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static rs.kunperooo.dailybot.utils.Converter.convert;
import static rs.kunperooo.dailybot.utils.Converter.convertCheckIns;
import static rs.kunperooo.dailybot.utils.Converter.convertMemberDtos;
import static rs.kunperooo.dailybot.utils.ScheduleUtils.calculateNextExecution;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RelationalDbCheckInService implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final CheckInQuestionRepository checkInQuestionRepository;
    private final CheckInNotificationScheduleRepository scheduleRepository;

    public void createCheckIn(String owner, String name, String introMessage, String outroMessage, @NonNull List<QuestionDto> questions, @NonNull List<MemberDto> members, Schedule schedule) {
        log.info("Creating new check-in for owner: {} with name: {} and {} questions",
                owner, name, questions);

        CheckInEntity checkIn = CheckInEntity.builder()
                .uuid(java.util.UUID.randomUUID())
                .owner(owner)
                .name(name)
                .introMessage(introMessage)
                .outroMessage(outroMessage)
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .members(convertMemberDtos(members))
                .build();
        for (CheckInQuestionEntity question : convert(questions)) {
            checkIn.addQuestion(question);
        }

        CheckInEntity savedCheckIn = checkInRepository.save(checkIn);
        log.info("Check-in created successfully with ID: {}", savedCheckIn.getId());

        if (schedule != null && schedule.getStartDate() != null) {
            saveSchedule(savedCheckIn, schedule);
        }
    }

    @Transactional(readOnly = true)
    public List<CheckInRestData> findByOwner(String owner) {
        log.debug("Finding all check-ins for owner: {}", owner);
        return convertCheckIns(checkInRepository.findByOwner(owner));
    }

    @Transactional(readOnly = true)
    public List<CheckInRestData> findAll() {
        log.debug("Finding all check-ins");
        return convertCheckIns(checkInRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<CheckInRestData> findByUuid(UUID uuid) {
        log.debug("Finding check-in by ID: {}", uuid);
        Optional<CheckInEntity> checkIn = checkInRepository.findByUuid(uuid);
        List<CheckInQuestionEntity> activeQuestions;
        if (checkIn.isPresent()) {
            activeQuestions = checkInQuestionRepository.getAllByCheckInIdAndIsActiveTrue(checkIn.get().getId());
            checkIn.get().setCheckInQuestions(activeQuestions);
        }
        return convert(checkIn);
    }

    @Transactional(readOnly = true)
    public Page<CheckInRestData> findByOwner(String owner, Pageable pageable) {
        log.debug("Finding check-ins for owner: {} with pagination", owner);
        return convert(checkInRepository.findByOwner(owner, pageable));
    }

    public void updateCheckIn(UUID uuid, String owner, String name, String introMessage, String outroMessage, @NonNull List<QuestionDto> questions, List<MemberDto> members, Schedule schedule) {
        log.info("Updating check-in for ID: {} and owner: {} with name: {} and {} questions",
                uuid, owner, name, questions);

        CheckInEntity checkIn = checkInRepository.findByUuidAndOwner(uuid, owner)
                .orElseThrow(() -> new RuntimeException("Check-in not found with ID: " + uuid + " for owner: " + owner));

        checkIn.setName(name);
        checkIn.setIntroMessage(introMessage);
        checkIn.setOutroMessage(outroMessage);

        List<UUID> uuids = questions.stream().map(QuestionDto::getUuid).toList();
        List<CheckInQuestionEntity> deactivatedQuestions = checkIn.getCheckInQuestions().stream()
                .filter(q -> !uuids.contains(q.getUuid()))
                .peek(q -> q.setActive(false))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<CheckInQuestionEntity> updatedQuestions = questions.stream()
                .map(q1 -> {
                    CheckInQuestionEntity currentQuestion = checkIn.getCheckInQuestions().stream().filter(q2 -> q1.getUuid().equals(q2.getUuid())).findFirst().orElse(null);
                    return CheckInQuestionEntity.builder()
                            .id(currentQuestion.getId())
                            .checkIn(checkIn)
                            .uuid(q1.getUuid())
                            .orderNumber(q1.getOrder())
                            .question(q1.getText())
                            .build();
                })
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

        updatedQuestions.addAll(deactivatedQuestions);

        checkIn.setCheckInQuestions(updatedQuestions);
        checkIn.setMembers(convertMemberDtos(members));
        checkIn.setLastUpdateDate(LocalDateTime.now());

        CheckInNotificationScheduleEntity scheduleEntity = checkIn.getNotificationSchedule()
                .setStartDate(schedule.getStartDate())
                .setTime(schedule.getTime())
                .setTimezone(schedule.getTimezone() != null ? schedule.getTimezone().getId() : null)
                .setFrequency(schedule.getFrequency())
                .setWeekDays(schedule.getDays() != null ? new ArrayList<>(schedule.getDays()) : new ArrayList<>())
                .setUpdatedAt(LocalDateTime.now());
        checkIn.setNotificationSchedule(scheduleEntity);
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

    private void saveSchedule(CheckInEntity checkIn, Schedule schedule) {
        if (schedule.getStartDate() == null) {
            log.debug("Skipping schedule save - startDate is null");
            return;
        }

        ZonedDateTime nextExecution = calculateNextExecution(schedule);

        CheckInNotificationScheduleEntity scheduleEntity = CheckInNotificationScheduleEntity.builder()
                .checkIn(checkIn)
                .startDate(schedule.getStartDate())
                .time(schedule.getTime())
                .timezone(schedule.getTimezone() != null ? schedule.getTimezone().getId() : null)
                .frequency(schedule.getFrequency())
                .weekDays(schedule.getDays() != null ? new ArrayList<>(schedule.getDays()) : new ArrayList<>())
                .nextExecution(nextExecution)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        scheduleRepository.save(scheduleEntity);
        log.info("Schedule saved for check-in ID: {} with next execution at: {}", checkIn.getId(), nextExecution);
    }

    private void updateSchedule(CheckInEntity checkIn, Schedule schedule) {
        Optional<CheckInNotificationScheduleEntity> existingSchedule = scheduleRepository.findByCheckInId(checkIn.getId())
                .stream()
                .findFirst();

        if (schedule == null || schedule.getStartDate() == null) {
            // Delete existing schedule if schedule is not provided or startDate is null
            existingSchedule.ifPresent(s -> {
                scheduleRepository.delete(s);
                log.info("Schedule deleted for check-in ID: {}", checkIn.getId());
            });
            return;
        }

        CheckInNotificationScheduleEntity scheduleEntity;
        if (existingSchedule.isPresent()) {
            // Update existing schedule
            scheduleEntity = existingSchedule.get();
            scheduleEntity.setStartDate(schedule.getStartDate());
            scheduleEntity.setTime(schedule.getTime());
            scheduleEntity.setTimezone(schedule.getTimezone() != null ? schedule.getTimezone().getId() : null);
            scheduleEntity.setFrequency(schedule.getFrequency());
            scheduleEntity.setWeekDays(schedule.getDays() != null ? new ArrayList<>(schedule.getDays()) : new ArrayList<>());
            scheduleEntity.setNextExecution(calculateNextExecution(schedule));
            scheduleEntity.setUpdatedAt(LocalDateTime.now());
            log.info("Schedule updated for check-in ID: {}", checkIn.getId());
        } else {
            // Create new schedule
            scheduleEntity = CheckInNotificationScheduleEntity.builder()
                    .checkIn(checkIn)
                    .startDate(schedule.getStartDate())
                    .time(schedule.getTime())
                    .timezone(schedule.getTimezone() != null ? schedule.getTimezone().getId() : null)
                    .frequency(schedule.getFrequency())
                    .weekDays(schedule.getDays() != null ? new ArrayList<>(schedule.getDays()) : new ArrayList<>())
                    .nextExecution(calculateNextExecution(schedule))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            log.info("Schedule created for check-in ID: {}", checkIn.getId());
        }

        scheduleRepository.save(scheduleEntity);
    }
}