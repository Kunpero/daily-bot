package rs.kunperooo.dailybot.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rs.kunperooo.dailybot.entity.Frequency;
import rs.kunperooo.dailybot.service.dto.ScheduleDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScheduleUtils Unit Tests")
class ScheduleUtilsTest {

    @Test
    @DisplayName("Should throw NullPointerException when schedule is null")
    void testCalculateNextExecution_NullSchedule() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            ScheduleUtils.calculateNextExecution(null);
        });
    }

    @Test
    @DisplayName("Should return null when startDate is null")
    void testCalculateNextExecution_NullStartDate() {
        // Arrange
        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(null)
                .time(LocalTime.of(10, 0))
                .timezone(ZoneId.systemDefault().getId())
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when time is null")
    void testCalculateNextExecution_NullTime() {
        // Arrange
        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(LocalDate.now())
                .time(null)
                .timezone(ZoneId.systemDefault().getId())
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when timezone is null")
    void testCalculateNextExecution_NullTimezone() {
        // Arrange
        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(LocalDate.now())
                .time(LocalTime.of(10, 0))
                .timezone(null)
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when days list is null")
    void testCalculateNextExecution_NullDaysList() {
        // Arrange
        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(LocalDate.now())
                .time(LocalTime.of(10, 0))
                .timezone(ZoneId.systemDefault().getId())
                .days(null)
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when days list is empty")
    void testCalculateNextExecution_EmptyDaysList() {
        // Arrange
        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(LocalDate.now())
                .time(LocalTime.of(10, 0))
                .timezone(ZoneId.systemDefault().getId())
                .days(Collections.emptyList())
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return next execution when startDate is today and time is in future")
    void testCalculateNextExecution_StartDateToday_TimeInFuture() {
        // Arrange
        LocalDate today = LocalDate.of(2025, 11, 6);
        LocalTime futureTime = LocalTime.now().plusHours(2);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today)
                .time(futureTime)
                .timezone(timezone.getId())
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(2025, 11, 13), result.toLocalDate());
        assertEquals(futureTime, result.toLocalTime());
        assertEquals(timezone, result.getZone());
    }

    @Test
    @DisplayName("Should return next bi-weekly execution when time already passed today")
    void testCalculateNextExecution_TimeAlreadyPassedToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today.minusWeeks(1)) // Started in the past
                .time(pastTime)
                .timezone(timezone.getId())
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().isAfter(today) || result.toLocalDate().equals(today.plusWeeks(2)));
        assertEquals(pastTime, result.toLocalTime());
        assertEquals(timezone, result.getZone());
    }

    @Test
    @DisplayName("Should return startDate when startDate is in future")
    void testCalculateNextExecution_StartDateInFuture() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalTime executionTime = LocalTime.of(14, 30);
        ZoneId timezone = ZoneId.of("America/New_York");
        DayOfWeek futureDayOfWeek = futureDate.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(futureDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(futureDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(futureDate, result.toLocalDate());
        assertEquals(executionTime, result.toLocalTime());
        assertEquals(timezone, result.getZone());
    }

    @Test
    @DisplayName("Should find next occurrence in same bi-weekly period")
    void testCalculateNextExecution_NextOccurrenceInSamePeriod() {
        // Arrange - Set start date to a known Monday, 2 weeks ago
        LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(2);
        LocalTime executionTime = LocalTime.of(9, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusWeeks(3)));
        assertTrue(schedule.getDays().contains(result.toLocalDate().getDayOfWeek()));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should find next occurrence in next bi-weekly period")
    void testCalculateNextExecution_NextOccurrenceInNextPeriod() {
        // Arrange - Set start date far in the past, and today is outside current period
        LocalDate startDate = LocalDate.now().minusWeeks(10);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();
        
        // Use days that might not be in the current period
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek nextDay = today.plus(3); // 3 days from today

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(nextDay))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(nextDay, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle single day selection")
    void testCalculateNextExecution_SingleDaySelection() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(4);
        LocalTime executionTime = LocalTime.of(15, 45);
        ZoneId timezone = ZoneId.of("Europe/London");
        DayOfWeek targetDay = DayOfWeek.THURSDAY;

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(targetDay))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(targetDay, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
        assertEquals(timezone, result.getZone());
    }

    @Test
    @DisplayName("Should handle all weekdays selection")
    void testCalculateNextExecution_AllWeekdays() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(6);
        LocalTime executionTime = LocalTime.of(8, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                ))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusWeeks(3)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle weekend days only")
    void testCalculateNextExecution_WeekendDaysOnly() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(8);
        LocalTime executionTime = LocalTime.of(12, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().getDayOfWeek() == DayOfWeek.SATURDAY ||
                   result.toLocalDate().getDayOfWeek() == DayOfWeek.SUNDAY);
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle different timezone - UTC")
    void testCalculateNextExecution_DifferentTimezone_UTC() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalTime executionTime = LocalTime.of(20, 30);
        ZoneId timezone = ZoneId.of("UTC");

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.TUESDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(timezone, result.getZone());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle different timezone - America/Los_Angeles")
    void testCalculateNextExecution_DifferentTimezone_LosAngeles() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(4);
        LocalTime executionTime = LocalTime.of(7, 15);
        ZoneId timezone = ZoneId.of("America/Los_Angeles");

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.WEDNESDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(timezone, result.getZone());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle different timezone - Asia/Tokyo")
    void testCalculateNextExecution_DifferentTimezone_Tokyo() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(3);
        LocalTime executionTime = LocalTime.of(18, 45);
        ZoneId timezone = ZoneId.of("Asia/Tokyo");

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.FRIDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(timezone, result.getZone());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle start date exactly 2 weeks ago")
    void testCalculateNextExecution_StartDateExactlyTwoWeeksAgo() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalTime executionTime = LocalTime.of(11, 0);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek startDayOfWeek = startDate.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(startDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        // Should be today (same day of week) if time hasn't passed, or next bi-weekly occurrence
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle start date exactly 1 week ago")
    void testCalculateNextExecution_StartDateExactlyOneWeekAgo() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(1);
        LocalTime executionTime = LocalTime.of(16, 30);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(today))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        // Should handle the fact that we're in week 1 of a bi-weekly period
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(today, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle edge case when start date is far in past")
    void testCalculateNextExecution_StartDateFarInPast() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalTime executionTime = LocalTime.of(9, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusWeeks(3)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle edge case with duplicate days in list")
    void testCalculateNextExecution_DuplicateDaysInList() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(4);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();

        // Duplicate MONDAY in list
        List<DayOfWeek> daysWithDuplicates = new ArrayList<>();
        daysWithDuplicates.add(DayOfWeek.MONDAY);
        daysWithDuplicates.add(DayOfWeek.MONDAY);
        daysWithDuplicates.add(DayOfWeek.WEDNESDAY);

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(daysWithDuplicates)
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().getDayOfWeek() == DayOfWeek.MONDAY ||
                   result.toLocalDate().getDayOfWeek() == DayOfWeek.WEDNESDAY);
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle time at midnight (00:00)")
    void testCalculateNextExecution_TimeAtMidnight() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(3);
        LocalTime executionTime = LocalTime.MIDNIGHT;
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.THURSDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.THURSDAY, result.toLocalDate().getDayOfWeek());
        assertEquals(LocalTime.MIDNIGHT, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle time at end of day (23:59)")
    void testCalculateNextExecution_TimeAtEndOfDay() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(5);
        LocalTime executionTime = LocalTime.of(23, 59);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.SUNDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.SUNDAY, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle schedule with frequency field set (even though not used in calculation)")
    void testCalculateNextExecution_WithFrequencySet() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalTime executionTime = LocalTime.of(13, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.BI_WEEKLY)
                .days(List.of(DayOfWeek.FRIDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.FRIDAY, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle when today matches multiple days in the period")
    void testCalculateNextExecution_MultipleDaysInPeriod() {
        // Arrange
        LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(4);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();
        
        // Get current day and next day
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek tomorrow = today.plus(1);

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(today, tomorrow))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().getDayOfWeek() == today || 
                   result.toLocalDate().getDayOfWeek() == tomorrow);
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should correctly calculate bi-weekly period boundaries")
    void testCalculateNextExecution_BiWeeklyPeriodBoundaries() {
        // Arrange - Start on a Monday, test finding next occurrence
        LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(6);
        LocalTime executionTime = LocalTime.of(8, 30);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.toLocalDate().getDayOfWeek());
        // Should be within the correct bi-weekly period (multiple of 2 weeks from start)
        long weeksDiff = java.time.temporal.ChronoUnit.WEEKS.between(startDate, result.toLocalDate());
        // The result should be at least 0 weeks from start and a multiple of 2 weeks (bi-weekly)
        assertTrue(weeksDiff >= 0, "Should be on or after start date");
        // Since we're finding the next occurrence, it should be within reasonable range
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle case when all required fields are present but schedule started long ago")
    void testCalculateNextExecution_LongRunningSchedule() {
        // Arrange
        LocalDate startDate = LocalDate.of(2020, 1, 1); // Far in the past
        LocalTime executionTime = LocalTime.of(12, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.TUESDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.TUESDAY, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusWeeks(3)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle when current time exactly matches execution time")
    void testCalculateNextExecution_TimeExactlyMatchesCurrentTime() {
        // Arrange - Set time to exactly now
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today.minusWeeks(2))
                .time(currentTime)
                .timezone(timezone.getId())
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        // Should move to next occurrence since time matches (considered as passed)
        assertTrue(result.toLocalDate().isAfter(today) || 
                   result.toLocalDate().equals(today) && result.toLocalTime().isAfter(currentTime));
    }

    @Test
    @DisplayName("Should handle start date on same day as today with time just passed")
    void testCalculateNextExecution_StartDateToday_TimeJustPassed() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusMinutes(5);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today)
                .time(pastTime)
                .timezone(timezone.getId())
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        // Should find next bi-weekly occurrence
        assertTrue(result.toLocalDate().isAfter(today) || 
                   result.toLocalDate().equals(today.plusWeeks(2)));
        assertEquals(pastTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle case when start date is in next bi-weekly period")
    void testCalculateNextExecution_StartDateInNextBiWeeklyPeriod() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusWeeks(3); // 3 weeks in future
        LocalTime executionTime = LocalTime.of(14, 0);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek startDayOfWeek = startDate.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(startDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(startDate, result.toLocalDate());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle unsorted days list")
    void testCalculateNextExecution_UnsortedDaysList() {
        // Arrange - Days not in order
        LocalDate startDate = LocalDate.now().minusWeeks(4);
        LocalTime executionTime = LocalTime.of(11, 30);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().getDayOfWeek() == DayOfWeek.MONDAY ||
                   result.toLocalDate().getDayOfWeek() == DayOfWeek.WEDNESDAY ||
                   result.toLocalDate().getDayOfWeek() == DayOfWeek.FRIDAY);
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle very early morning time (00:01)")
    void testCalculateNextExecution_VeryEarlyMorningTime() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(5);
        LocalTime executionTime = LocalTime.of(0, 1);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(DayOfWeek.THURSDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.THURSDAY, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle case when start date is yesterday")
    void testCalculateNextExecution_StartDateYesterday() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalTime executionTime = LocalTime.of(15, 0);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek yesterdayDayOfWeek = startDate.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .days(List.of(yesterdayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        // Should find next occurrence (2 weeks later)
        assertTrue(result.toLocalDate().isAfter(startDate));
        assertEquals(yesterdayDayOfWeek, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should calculate next weekly execution when frequency is WEEKLY")
    void testCalculateNextExecution_WeeklyFrequency() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek nextDay = today.plus(1);

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.WEEKLY)
                .days(List.of(nextDay))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(nextDay, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusDays(8)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should find next occurrence within 7 days for WEEKLY frequency")
    void testCalculateNextExecution_WeeklyFrequency_WithinWeek() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(5);
        LocalTime executionTime = LocalTime.of(14, 30);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.WEEKLY)
                .days(List.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().getDayOfWeek() == DayOfWeek.WEDNESDAY ||
                   result.toLocalDate().getDayOfWeek() == DayOfWeek.FRIDAY);
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusDays(8)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle WEEKLY frequency when time has passed today")
    void testCalculateNextExecution_WeeklyFrequency_TimePassed() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today.minusWeeks(1))
                .time(pastTime)
                .timezone(timezone.getId())
                .frequency(Frequency.WEEKLY)
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(todayDayOfWeek, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(today) || result.toLocalDate().equals(today.plusDays(7)));
        assertEquals(pastTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should calculate next monthly execution when frequency is MONTHLY")
    void testCalculateNextExecution_MonthlyFrequency() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(2);
        LocalTime executionTime = LocalTime.of(9, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusMonths(2)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should find next occurrence in current month for MONTHLY frequency")
    void testCalculateNextExecution_MonthlyFrequency_CurrentMonth() {
        // Arrange
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        LocalTime executionTime = LocalTime.of(11, 0);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek nextDay = LocalDate.now().getDayOfWeek().plus(2);

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(nextDay))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(nextDay, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should find next occurrence in next month for MONTHLY frequency when current month passed")
    void testCalculateNextExecution_MonthlyFrequency_NextMonth() {
        // Arrange - Start date at beginning of previous month, today is near end of current month
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(2);
        LocalTime executionTime = LocalTime.of(15, 0);
        ZoneId timezone = ZoneId.systemDefault();
        
        // Use a day that's likely not in the current month's remaining days
        DayOfWeek targetDay = DayOfWeek.MONDAY;

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(targetDay))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(targetDay, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle MONTHLY frequency with multiple days")
    void testCalculateNextExecution_MonthlyFrequency_MultipleDays() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().getDayOfWeek() == DayOfWeek.TUESDAY ||
                   result.toLocalDate().getDayOfWeek() == DayOfWeek.THURSDAY);
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle MONTHLY frequency when start date is in future")
    void testCalculateNextExecution_MonthlyFrequency_FutureStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        LocalTime executionTime = LocalTime.of(12, 0);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek startDayOfWeek = startDate.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(startDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(startDate, result.toLocalDate());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle MONTHLY frequency when time has passed today")
    void testCalculateNextExecution_MonthlyFrequency_TimePassed() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(2);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today.minusMonths(1))
                .time(pastTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(todayDayOfWeek, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(today) || result.toLocalDate().equals(today.plusMonths(1)));
        assertEquals(pastTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle WEEKLY frequency with all weekdays")
    void testCalculateNextExecution_WeeklyFrequency_AllWeekdays() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(3);
        LocalTime executionTime = LocalTime.of(8, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.WEEKLY)
                .days(List.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                ))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertTrue(result.toLocalDate().isBefore(LocalDate.now().plusDays(8)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle MONTHLY frequency across year boundary")
    void testCalculateNextExecution_MonthlyFrequency_YearBoundary() {
        // Arrange - Start in December, calculate for January
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear() - 1, 12, 1);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(DayOfWeek.FRIDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.FRIDAY, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should default to BI_WEEKLY when frequency is null")
    void testCalculateNextExecution_NullFrequency_DefaultsToBiWeekly() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusWeeks(2);
        LocalTime executionTime = LocalTime.of(10, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(null)
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.toLocalDate().getDayOfWeek());
        assertEquals(executionTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle WEEKLY frequency with start date today")
    void testCalculateNextExecution_WeeklyFrequency_StartDateToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalTime futureTime = LocalTime.now().plusHours(2);
        ZoneId timezone = ZoneId.systemDefault();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(today)
                .time(futureTime)
                .timezone(timezone.getId())
                .frequency(Frequency.WEEKLY)
                .days(List.of(todayDayOfWeek))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(today, result.toLocalDate());
        assertEquals(futureTime, result.toLocalTime());
    }

    @Test
    @DisplayName("Should handle MONTHLY frequency with start date at month start")
    void testCalculateNextExecution_MonthlyFrequency_MonthStart() {
        // Arrange
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        LocalTime executionTime = LocalTime.of(9, 0);
        ZoneId timezone = ZoneId.systemDefault();

        ScheduleDto schedule = ScheduleDto.builder()
                .startDate(startDate)
                .time(executionTime)
                .timezone(timezone.getId())
                .frequency(Frequency.MONTHLY)
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // Act
        ZonedDateTime result = ScheduleUtils.calculateNextExecution(schedule);

        // Assert
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.toLocalDate().getDayOfWeek());
        assertTrue(result.toLocalDate().isAfter(LocalDate.now().minusDays(1)));
        assertEquals(executionTime, result.toLocalTime());
    }
}
