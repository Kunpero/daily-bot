package rs.kunperooo.dailybot.config;

import com.google.gson.Gson;
import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.util.json.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
@Slf4j
public class SlackApiConfig {

    @Value("${slack.api.timeout:30000}")
    private int apiTimeout;

    @Value("${slack.api.retry.attempts:3}")
    private int retryAttempts;

    @Bean
    public Slack slackClient() {
        log.info("Configuring Slack API client with timeout: {}ms, retry attempts: {}",
                apiTimeout, retryAttempts);

        SlackConfig config = new SlackConfig();
        config.setHttpClientReadTimeoutMillis(apiTimeout);
        config.setHttpClientWriteTimeoutMillis(apiTimeout);
        return Slack.getInstance(config);
    }

    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter(Gson gson) {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }

    @Bean
    public Gson gson() {
        return GsonFactory.createSnakeCase();
    }
}