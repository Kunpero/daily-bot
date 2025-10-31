package rs.kunperooo.dailybot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Converter
public class WeekDayListJsonConverter implements AttributeConverter<List<DayOfWeek>, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<DayOfWeek> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) {
                return "[]";
            }
            // Convert DayOfWeek to string names (e.g., "MONDAY", "TUESDAY")
            List<String> dayNames = attribute.stream()
                    .map(DayOfWeek::name)
                    .toList();
            return mapper.writeValueAsString(dayNames);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting week days list to JSON", e);
        }
    }

    @Override
    public List<DayOfWeek> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank() || dbData.equals("[]")) {
                return new ArrayList<>();
            }
            // Read array of strings and convert to DayOfWeek
            List<String> dayNames = mapper.readValue(dbData, new TypeReference<List<String>>() {});
            return dayNames.stream()
                    .map(DayOfWeek::valueOf)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Error reading week days list from JSON", e);
        }
    }
}
