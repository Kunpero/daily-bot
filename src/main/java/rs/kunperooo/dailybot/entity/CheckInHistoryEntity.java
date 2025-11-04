package rs.kunperooo.dailybot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "check_in_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(targetEntity = CheckInEntity.class)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckInEntity checkIn;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "checkInHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CheckInQuestionInHistoryEntity> checkInQuestionInHistory = new LinkedList<>();

    public void addQuestionInHistory(CheckInQuestionInHistoryEntity question) {
        question.setCheckInHistory(this);
        checkInQuestionInHistory.add(question);
    }
}
