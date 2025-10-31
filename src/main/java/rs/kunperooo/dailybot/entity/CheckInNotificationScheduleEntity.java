package rs.kunperooo.dailybot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import rs.kunperooo.dailybot.utils.WeekDayListJsonConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "check_in_notification_schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInNotificationScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckInEntity checkIn;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "frequency", nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    private Frequency frequency;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "next_execution_at")
    private LocalDateTime nextExecutionAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "week_days", length = 100)
    @Convert(converter = WeekDayListJsonConverter.class)
    @Builder.Default
    private List<DayOfWeek> weekDays = new ArrayList<>();
}
