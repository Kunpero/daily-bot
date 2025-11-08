package rs.kunperooo.dailybot.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.kunperooo.dailybot.entity.CheckInAnswerEntity;
import rs.kunperooo.dailybot.entity.CheckInEntity;
import rs.kunperooo.dailybot.entity.CheckInHistoryEntity;
import rs.kunperooo.dailybot.entity.CheckInNotificationScheduleEntity;
import rs.kunperooo.dailybot.entity.CheckInQuestionEntity;
import rs.kunperooo.dailybot.entity.CheckInQuestionInHistoryEntity;
import rs.kunperooo.dailybot.repository.CheckInAnswerRepository;
import rs.kunperooo.dailybot.repository.CheckInHistoryRepository;
import rs.kunperooo.dailybot.repository.CheckInNotificationScheduleRepository;
import rs.kunperooo.dailybot.repository.CheckInQuestionInHistoryRepository;
import rs.kunperooo.dailybot.repository.CheckInQuestionRepository;
import rs.kunperooo.dailybot.repository.CheckInRepository;
import rs.kunperooo.dailybot.service.cache.SlackUserCacheService;
import rs.kunperooo.dailybot.service.dto.CheckInDataDto;
import rs.kunperooo.dailybot.service.dto.MemberDto;
import rs.kunperooo.dailybot.service.dto.QuestionDto;
import rs.kunperooo.dailybot.service.dto.SaveAnswersDto;
import rs.kunperooo.dailybot.service.dto.ScheduleDto;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;
import rs.kunperooo.dailybot.service.dto.history.CheckInHistoryDto;
import rs.kunperooo.dailybot.utils.Converter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static rs.kunperooo.dailybot.utils.Converter.convertToDtoList;
import static rs.kunperooo.dailybot.utils.Converter.convertToMemberEntityList;
import static rs.kunperooo.dailybot.utils.Converter.convertToHistoryDtoListFromQuestions;
import static rs.kunperooo.dailybot.utils.ScheduleUtils.calculateNextExecution;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RelationalDbCheckInService implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final CheckInQuestionRepository checkInQuestionRepository;
    private final CheckInNotificationScheduleRepository scheduleRepository;
    private final CheckInAnswerRepository checkInAnswerRepository;
    private final CheckInHistoryRepository checkInHistoryRepository;
    private final CheckInQuestionInHistoryRepository checkInQuestionInHistoryRepository;
    private final CheckInNotificationScheduleRepository checkInNotificationScheduleRepository;
    private final SlackUserCacheService slackUserCacheService;

    public void createCheckIn(String owner, String name, String introMessage, String outroMessage, @NonNull List<QuestionDto> questions, @NonNull List<MemberDto> members, ScheduleDto schedule) {
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
                .members(convertToMemberEntityList(members))
                .build();
        for (CheckInQuestionEntity question : Converter.convertToEntityList(questions)) {
            checkIn.addQuestion(question);
        }

        CheckInEntity savedCheckIn = checkInRepository.save(checkIn);
        log.info("Check-in created successfully with ID: {}", savedCheckIn.getId());

        if (schedule != null && schedule.getStartDate() != null) {
            saveSchedule(savedCheckIn, schedule);
        }
    }

    @Transactional(readOnly = true)
    public List<CheckInDataDto> findByOwner(String owner) {
        log.debug("Finding all check-ins for owner: {}", owner);
        return convertToDtoList(checkInRepository.findByOwner(owner));
    }

    @Transactional(readOnly = true)
    public List<CheckInDataDto> findAll() {
        log.debug("Finding all check-ins");
        return convertToDtoList(checkInRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<CheckInDataDto> findByUuid(UUID uuid) {
        log.debug("Finding check-in by ID: {}", uuid);
        Optional<CheckInEntity> checkIn = checkInRepository.findByUuid(uuid);
        List<CheckInQuestionEntity> activeQuestions;
        if (checkIn.isPresent()) {
            activeQuestions = checkInQuestionRepository.getAllByCheckInIdAndIsActiveTrue(checkIn.get().getId());
            checkIn.get().setCheckInQuestions(activeQuestions);
        }
        return Converter.convertToDto(checkIn);
    }

    @Transactional(readOnly = true)
    public Page<CheckInDataDto> findByOwner(String owner, Pageable pageable) {
        log.debug("Finding check-ins for owner: {} with pagination", owner);
        return Converter.convertToDtoPage(checkInRepository.findByOwner(owner, pageable));
    }

    public void updateCheckIn(UUID uuid, String owner, String name, String introMessage, String outroMessage, @NonNull List<QuestionDto> questions, List<MemberDto> members, ScheduleDto schedule) {
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
        checkIn.setMembers(convertToMemberEntityList(members));
        checkIn.setLastUpdateDate(LocalDateTime.now());

        CheckInNotificationScheduleEntity scheduleEntity = checkIn.getNotificationSchedule()
                .setStartDate(schedule.getStartDate())
                .setTime(schedule.getTime())
                .setTimezone(schedule.getTimezone())
                .setFrequency(schedule.getFrequency())
                .setWeekDays(schedule.getDays() != null ? new ArrayList<>(schedule.getDays()) : new ArrayList<>())
                .setNextExecution(calculateNextExecution(schedule))
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

    @Override
    public void saveOrUpdateAnswers(SaveAnswersDto dto) {
        List<CheckInAnswerEntity> answers = dto.getAnswers().stream()
                .map(a -> {
                    CheckInQuestionInHistoryEntity questionInHistory = checkInQuestionInHistoryRepository.findByUuid(a.getQuestionInHistoryUuid());
                    CheckInAnswerEntity answer = checkInAnswerRepository.findByUuid(a.getUuid());

                    return CheckInAnswerEntity.builder()
                            .id(answer != null ? answer.getId() : null)
                            .uuid(answer != null ? answer.getUuid() : java.util.UUID.randomUUID())
                            .userId(dto.getUserId())
                            .checkInQuestionInHistory(questionInHistory)
                            .answer(a.getAnswer())
                            .build();
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        checkInAnswerRepository.saveAll(answers);
    }

    @Override
    public Optional<CheckInHistoryEntity> findHistoryByUuid(UUID uuid) {
        return checkInHistoryRepository.findByUuid(uuid);
    }

    @Override
    public List<CheckInDataDto> findByNextExecutionIsBefore(ZonedDateTime nextExecutionBefore, Pageable pageable) {
        return checkInNotificationScheduleRepository.findByNextExecutionIsBefore(nextExecutionBefore, pageable).stream().map(n -> Converter.convertToDto(n.getCheckIn())).toList();
    }

    @NotNull
    public UUID saveHistory(CheckInDataDto checkIn) {
        CheckInEntity checkInEntity = checkInRepository.findByUuid(checkIn.getUuid()).get();
        UUID historyUuid = UUID.randomUUID();
        List<CheckInQuestionInHistoryEntity> questionsInHistory = checkInEntity.getCheckInQuestions().stream()
                .map(q -> CheckInQuestionInHistoryEntity.builder()
                        .uuid(UUID.randomUUID())
                        .checkInQuestion(q)
                        .build())
                .toList();
        CheckInHistoryEntity history = CheckInHistoryEntity.builder()
                .checkIn(checkInEntity)
                .createdAt(LocalDateTime.now())
                .uuid(historyUuid)
                .build();

        for (CheckInQuestionInHistoryEntity q : questionsInHistory) {
            history.addQuestionInHistory(q);
        }
        checkInHistoryRepository.save(history);
        return historyUuid;
    }

    @Transactional
    @Override
    public void saveNextExecution(UUID checkInUuid, ZonedDateTime nextExecution) {
        Optional<CheckInEntity> checkIn = checkInRepository.findByUuid(checkInUuid);
        CheckInNotificationScheduleEntity schedule = checkIn.get().getNotificationSchedule();
        schedule.setNextExecution(nextExecution);
        checkInNotificationScheduleRepository.save(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInHistoryDto> getHistory(UUID checkInUuid) {
        log.debug("Finding check-in history for UUID: {}", checkInUuid);
        
        // Get all histories for this check-in
        List<CheckInHistoryEntity> histories = checkInHistoryRepository.findByCheckInUuid(checkInUuid);
        
        if (histories == null || histories.isEmpty()) {
            log.debug("No history found for check-in UUID: {}", checkInUuid);
            return new LinkedList<>();
        }

        // Get all questions for all histories
        List<CheckInQuestionInHistoryEntity> allQuestions = new LinkedList<>();
        for (CheckInHistoryEntity history : histories) {
            List<CheckInQuestionInHistoryEntity> questions = checkInQuestionInHistoryRepository.findByCheckInHistoryUuid(history.getUuid());
            allQuestions.addAll(questions);
        }

        if (allQuestions.isEmpty()) {
            log.debug("No questions found for check-in UUID: {}", checkInUuid);
            return new LinkedList<>();
        }

        return convertToHistoryDtoListFromQuestions(allQuestions, slackUserCacheService.getAllUsers());
    }

    private void saveSchedule(CheckInEntity checkIn, ScheduleDto schedule) {
        if (schedule.getStartDate() == null) {
            log.debug("Skipping schedule save - startDate is null");
            return;
        }

        ZonedDateTime nextExecution = calculateNextExecution(schedule);

        CheckInNotificationScheduleEntity scheduleEntity = CheckInNotificationScheduleEntity.builder()
                .checkIn(checkIn)
                .startDate(schedule.getStartDate())
                .time(schedule.getTime())
                .timezone(schedule.getTimezone())
                .frequency(schedule.getFrequency())
                .weekDays(schedule.getDays() != null ? new ArrayList<>(schedule.getDays()) : new ArrayList<>())
                .nextExecution(nextExecution)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        scheduleRepository.save(scheduleEntity);
        log.info("Schedule saved for check-in ID: {} with next execution at: {}", checkIn.getId(), nextExecution);
    }

    private void updateSchedule(CheckInEntity checkIn, ScheduleDto schedule) {
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
            scheduleEntity.setTimezone(schedule.getTimezone());
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
                    .timezone(schedule.getTimezone())
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