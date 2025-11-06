package rs.kunperooo.dailybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DailyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyBotApplication.class, args);
    }

}
