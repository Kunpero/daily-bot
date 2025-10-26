package rs.kunperooo.dailybot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "check_in_question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
