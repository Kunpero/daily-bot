package rs.kunperooo.dailybot.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("user", principal.getAttribute("name"));
            model.addAttribute("email", principal.getAttribute("email"));
            model.addAttribute("userId", principal.getAttribute("sub"));
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}