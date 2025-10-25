package rs.kunperooo.dailybot.controller.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CheckInRestData {
    private UUID uuid;

    private String owner;

    private String name;

    private LocalDateTime creationDate;

    private LocalDateTime lastUpdateDate;

    private String introMessage;

    private String outroMessage;
}
