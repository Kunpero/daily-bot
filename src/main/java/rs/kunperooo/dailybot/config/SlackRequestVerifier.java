package rs.kunperooo.dailybot.config;

import com.slack.api.app_backend.SlackSignature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class SlackRequestVerifier {

    private final SlackSignature.Verifier verifier;

    public SlackRequestVerifier(@Value("${slack.api.signing.secret}") String signingSecret) {
        this.verifier = new SlackSignature.Verifier(new SlackSignature.Generator(signingSecret));
    }

    public boolean isValid(HttpServletRequest request, String body) {
        String timestamp = request.getHeader(SlackSignature.HeaderNames.X_SLACK_REQUEST_TIMESTAMP);
        String signature = request.getHeader(SlackSignature.HeaderNames.X_SLACK_SIGNATURE);

        return verifier.isValid(timestamp, body, signature);
    }

    public static String readBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

