package rs.kunperooo.dailybot.utils;

import lombok.extern.slf4j.Slf4j;
import rs.kunperooo.dailybot.controller.dto.ScheduleRest;
import rs.kunperooo.dailybot.service.dto.ScheduleDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class ScheduleUtils {
    public static ZonedDateTime calculateNextExecution(ScheduleDto schedule) {
        if (schedule.getStartDate() == null || schedule.getTime() == null || schedule.getTimezone() == null) {
            log.debug("Cannot calculate next execution - missing required fields");
            return null;
        }

        if (schedule.getDays() == null || schedule.getDays().isEmpty()) {
            log.debug("Cannot calculate next execution - no week days specified");
            return null;
        }

        LocalDate startDate = schedule.getStartDate();
        LocalTime time = schedule.getTime();
        ZoneId timezone = ZoneId.of(schedule.getTimezone());
        List<DayOfWeek> weekDays = schedule.getDays();

        // Get current date/time in the schedule's timezone
        ZonedDateTime now = ZonedDateTime.now(timezone);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // Sort week days for easier processing
        Set<DayOfWeek> sortedDays = new TreeSet<>(weekDays);

        // Start from today or startDate, whichever is later
        LocalDate searchDate = today.isBefore(startDate) ? startDate : today;

        // For BI_WEEKLY frequency, find the next occurrence
        LocalDate nextDate = findNextBiWeeklyDate(searchDate, sortedDays, startDate);

        // If we found a date today but time has passed, try next occurrence
        if (nextDate != null && nextDate.equals(today) && time.isBefore(currentTime)) {
            nextDate = findNextBiWeeklyDate(searchDate.plusDays(1), sortedDays, startDate);
        }

        if (nextDate == null) {
            log.debug("Could not find next execution date");
            return null;
        }

        // Combine date and time in the specified timezone
        return ZonedDateTime.of(nextDate, time, timezone);
    }

    private static LocalDate findNextBiWeeklyDate(LocalDate fromDate, Set<DayOfWeek> weekDays, LocalDate startDate) {
        // Calculate weeks since start date
        long weeksSinceStart = ChronoUnit.WEEKS.between(startDate, fromDate);

        // For bi-weekly, we want to start from the bi-weekly period
        long biWeeklyPeriodsSinceStart = weeksSinceStart / 2;
        LocalDate periodStartDate = startDate.plusWeeks(biWeeklyPeriodsSinceStart * 2);

        // Check current period first
        LocalDate nextDate = findNextDayInPeriod(periodStartDate, fromDate, weekDays);
        if (nextDate != null) {
            return nextDate;
        }

        // If not found in current period, try next bi-weekly period
        return findNextDayInPeriod(periodStartDate.plusWeeks(2), periodStartDate.plusWeeks(2), weekDays);
    }

    private static LocalDate findNextDayInPeriod(LocalDate periodStart, LocalDate fromDate, Set<DayOfWeek> weekDays) {
        LocalDate searchDate = fromDate.isBefore(periodStart) ? periodStart : fromDate;

        // Look within the next 14 days (2 weeks)
        for (int i = 0; i < 14; i++) {
            LocalDate candidate = searchDate.plusDays(i);
            DayOfWeek dayOfWeek = candidate.getDayOfWeek();

            if (weekDays.contains(dayOfWeek)) {
                return candidate;
            }
        }

        return null;
    }
}
