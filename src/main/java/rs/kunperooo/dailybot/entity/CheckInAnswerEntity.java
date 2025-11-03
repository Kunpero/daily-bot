package rs.kunperooo.dailybot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "check_in_answer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "answer", updatable = true)
    private String answer;

    @ManyToOne(targetEntity = CheckInQuestionEntity.class)
    @JoinColumn(name = "question_id")
    private CheckInQuestionEntity checkInQuestion;
}
