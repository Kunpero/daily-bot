package rs.kunperooo.dailybot.filter;

import com.slack.api.app_backend.SlackSignature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.kunperooo.dailybot.config.SlackRequestVerifier;

import java.io.IOException;
import java.util.stream.Collectors;

import static com.nimbusds.oauth2.sdk.http.HTTPResponse.SC_UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
public class SlackRequestVerifierFilter extends OncePerRequestFilter {
    private final SlackRequestVerifier slackRequestVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            AuthenticationRequestWrapper wrappedRequest = new AuthenticationRequestWrapper(request);
            String body = wrappedRequest.getReader().lines().collect(Collectors.joining());
            boolean isValid = slackRequestVerifier.isValid(wrappedRequest, body);
            if (!isValid) {
                String signature = wrappedRequest.getHeader(SlackSignature.HeaderNames.X_SLACK_SIGNATURE);
                log.debug("An invalid X-Slack-Signature detected - {}", signature);
                response.setStatus(SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(wrappedRequest, response);
        } catch (Exception ex) {
            log.error("System error", ex);
            throw ex;
        }
    }
}
