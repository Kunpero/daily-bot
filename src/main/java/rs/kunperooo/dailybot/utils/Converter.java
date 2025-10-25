package rs.kunperooo.dailybot.utils;

import org.springframework.data.domain.Page;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.entity.CheckInEntity;

import java.util.List;
import java.util.Optional;

public class Converter {

    public static List<CheckInRestData> convert(List<CheckInEntity> checkInList) {
        return checkInList.stream()
                .map(Converter::convert)
                .toList();
    }

    public static Optional<CheckInRestData> convert(Optional<CheckInEntity> checkIn) {
        return checkIn.map(Converter::convert);
    }

    public static CheckInRestData convert(CheckInEntity checkIn) {
        return CheckInRestData.builder()
                .uuid(checkIn.getUuid())
                .owner(checkIn.getOwner())
                .name(checkIn.getName())
                .lastUpdateDate(checkIn.getLastUpdateDate())
                .creationDate(checkIn.getCreationDate())
                .introMessage(checkIn.getIntroMessage())
                .outroMessage(checkIn.getOutroMessage())
                .build();
    }

    public static Page<CheckInRestData> convert(Page<CheckInEntity> checkInPage) {
        return checkInPage.map(Converter::convert);
    }
}
