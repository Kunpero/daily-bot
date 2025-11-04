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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "check_in_question_in_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInQuestionInHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(targetEntity = CheckInHistoryEntity.class)
    @JoinColumn(name = "check_in_history_id", nullable = false)
    private CheckInHistoryEntity checkInHistory;

    @ManyToOne(targetEntity = CheckInQuestionEntity.class)
    @JoinColumn(name = "check_in_question_id", nullable = false)
    private CheckInQuestionEntity checkInQuestion;

    @OneToMany(mappedBy = "checkInQuestionInHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("userId")
    private List<CheckInAnswerEntity> checkInAnswers = new LinkedList<>();
}
