package rs.kunperooo.dailybot.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void init() {
        log.info("Initializing Slack user cache asynchronously...");
        initializeCache();
    }

    private void initializeCache() {
        if (cache != null) {
            return; // Already initialized
        }

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
        log.info("Slack user cache initialized successfully with {} users", usersMap.size());
    }

    private LoadingCache<String, SlackUserDto> getCache() {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    log.warn("Cache not yet initialized, initializing synchronously...");
                    initializeCache();
                }
            }
        }
        return cache;
    }

    @SneakyThrows
    public SlackUserDto getUser(String slackUserId) {
        return getCache().get(slackUserId);
    }

    public Map<String, SlackUserDto> getAllUsers() {
        LoadingCache<String, SlackUserDto> currentCache = getCache();
        return currentCache != null ? currentCache.asMap() : Map.of();
    }
}
