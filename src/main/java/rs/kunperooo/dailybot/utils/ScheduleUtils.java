package rs.kunperooo.dailybot.utils;

import lombok.extern.slf4j.Slf4j;
import rs.kunperooo.dailybot.entity.Frequency;
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

        // Get frequency, default to BI_WEEKLY if not specified
        Frequency frequency = schedule.getFrequency() != null ? schedule.getFrequency() : Frequency.BI_WEEKLY;

        // Find the next occurrence based on frequency
        LocalDate nextDate = findNextDateByFrequency(searchDate, sortedDays, startDate, frequency);

        // If we found a date today but time has passed, try next occurrence
        if (nextDate != null && nextDate.equals(today) && time.isBefore(currentTime)) {
            nextDate = findNextDateByFrequency(searchDate.plusDays(1), sortedDays, startDate, frequency);
        }

        if (nextDate == null) {
            log.debug("Could not find next execution date");
            return null;
        }

        // Combine date and time in the specified timezone
        return ZonedDateTime.of(nextDate, time, timezone);
    }

    private static LocalDate findNextDateByFrequency(LocalDate fromDate, Set<DayOfWeek> weekDays, LocalDate startDate, Frequency frequency) {
        switch (frequency) {
            case WEEKLY:
                return findNextWeeklyDate(fromDate, weekDays);
            case BI_WEEKLY:
                return findNextBiWeeklyDate(fromDate, weekDays, startDate);
            case MONTHLY:
                return findNextMonthlyDate(fromDate, weekDays, startDate);
            default:
                log.warn("Unknown frequency: {}, defaulting to BI_WEEKLY", frequency);
                return findNextBiWeeklyDate(fromDate, weekDays, startDate);
        }
    }

    private static LocalDate findNextWeeklyDate(LocalDate fromDate, Set<DayOfWeek> weekDays) {
        // For weekly, look within the next 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate candidate = fromDate.plusDays(i);
            DayOfWeek dayOfWeek = candidate.getDayOfWeek();

            if (weekDays.contains(dayOfWeek)) {
                return candidate;
            }
        }

        return null;
    }

    private static LocalDate findNextBiWeeklyDate(LocalDate fromDate, Set<DayOfWeek> weekDays, LocalDate startDate) {
        // Calculate weeks since start date
        long weeksSinceStart = ChronoUnit.WEEKS.between(startDate, fromDate);

        // For bi-weekly, we want to start from the bi-weekly period
        long biWeeklyPeriodsSinceStart = weeksSinceStart / 2;
        LocalDate periodStartDate = startDate.plusWeeks(biWeeklyPeriodsSinceStart * 2);

        // Check current period first
        LocalDate nextDate = findNextDayInPeriod(periodStartDate, fromDate, weekDays, 14);
        if (nextDate != null) {
            return nextDate;
        }

        // If not found in current period, try next bi-weekly period
        return findNextDayInPeriod(periodStartDate.plusWeeks(2), periodStartDate.plusWeeks(2), weekDays, 14);
    }

    private static LocalDate findNextMonthlyDate(LocalDate fromDate, Set<DayOfWeek> weekDays, LocalDate startDate) {
        // Calculate months since start date
        long monthsSinceStart = ChronoUnit.MONTHS.between(startDate, fromDate);
        
        // For monthly, we want to find the next occurrence in the current or next month period
        LocalDate periodStartDate = startDate.plusMonths(monthsSinceStart);
        
        // Check current month period first
        LocalDate nextDate = findNextDayInMonthlyPeriod(periodStartDate, fromDate, weekDays);
        if (nextDate != null) {
            return nextDate;
        }
        
        // If not found in current month, try next month period
        return findNextDayInMonthlyPeriod(periodStartDate.plusMonths(1), periodStartDate.plusMonths(1), weekDays);
    }

    private static LocalDate findNextDayInPeriod(LocalDate periodStart, LocalDate fromDate, Set<DayOfWeek> weekDays, int periodDays) {
        LocalDate searchDate = fromDate.isBefore(periodStart) ? periodStart : fromDate;

        // Look within the specified period days
        for (int i = 0; i < periodDays; i++) {
            LocalDate candidate = searchDate.plusDays(i);
            DayOfWeek dayOfWeek = candidate.getDayOfWeek();

            if (weekDays.contains(dayOfWeek)) {
                return candidate;
            }
        }

        return null;
    }

    private static LocalDate findNextDayInMonthlyPeriod(LocalDate periodStart, LocalDate fromDate, Set<DayOfWeek> weekDays) {
        LocalDate searchDate = fromDate.isBefore(periodStart) ? periodStart : fromDate;
        
        // Get the end of the month for the period start
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
        
        // Look within the current month
        LocalDate candidate = searchDate;
        while (!candidate.isAfter(periodEnd)) {
            DayOfWeek dayOfWeek = candidate.getDayOfWeek();
            
            if (weekDays.contains(dayOfWeek)) {
                return candidate;
            }
            
            candidate = candidate.plusDays(1);
        }

        return null;
    }
}
