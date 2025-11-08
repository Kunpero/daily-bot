package rs.kunperooo.dailybot.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.kunperooo.dailybot.service.SlackApiService;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackUserCacheService {
    private final SlackApiService slackApiService;
    private LoadingCache<String, SlackUserDto> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<>() {
                    @Override
                    public SlackUserDto load(String key) throws Exception {
                        return slackApiService.getUser(key);
                    }
                });

        Map<String, SlackUserDto> usersMap = new ConcurrentHashMap<>();
        for (SlackUserDto slackUserDto : slackApiService.getActiveUsers()) {
            usersMap.put(slackUserDto.getId(), slackUserDto);
        }
        cache.putAll(usersMap);
    }

    @SneakyThrows
    public SlackUserDto getUser(String slackUserId) {
        return cache.get(slackUserId);
    }

    public Map<String, SlackUserDto> getAllUsers() {
        return cache.asMap();
    }
}
