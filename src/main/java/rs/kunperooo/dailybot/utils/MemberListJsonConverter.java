package rs.kunperooo.dailybot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import rs.kunperooo.dailybot.entity.Member;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class MemberListJsonConverter implements AttributeConverter<List<Member>, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Member> attribute) {
        try {
            return attribute == null ? "[]" : mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting member list to JSON", e);
        }
    }

    @Override
    public List<Member> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null || dbData.isBlank()
                    ? new ArrayList<>()
                    : mapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading member list from JSON", e);
        }
    }
}
